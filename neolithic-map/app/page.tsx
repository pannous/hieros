"use client";

import { PointerEvent, useEffect, useMemo, useRef, useState } from "react";

const SVG_WIDTH = 2421.2596;
const SVG_HEIGHT = 2480.3192;

type Video = {
  id: string;
  title: string;
  url: string;
  durationString?: string;
  thumbnail?: string;
  matchedSites: string[];
  playlists?: string[];
};

type Site = {
  id: string;
  name: string;
  x: number;
  y: number;
  coordinateQuality?: string;
  aliases: string[];
  videoIds: string[];
  source: string;
  wikipediaUrl?: string;
};

function normalize(value: string) {
  return value
    .normalize("NFKD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase();
}

export default function Home() {
  const [sites, setSites] = useState<Site[]>([]);
  const [videos, setVideos] = useState<Video[]>([]);
  const [selectedSiteId, setSelectedSiteId] = useState<string | null>(null);
  const [query, setQuery] = useState("");
  const [videoOnly, setVideoOnly] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [zoom, setZoom] = useState(1);
  const [pan, setPan] = useState({ x: 0, y: 0 });
  const dragRef = useRef<{ pointerId: number; x: number; y: number } | null>(null);
  const pointersRef = useRef(new Map<number, { x: number; y: number }>());
  const pinchRef = useRef<{
    distance: number;
    center: { x: number; y: number };
    zoom: number;
    pan: { x: number; y: number };
  } | null>(null);

  useEffect(() => {
    async function loadData() {
      const [sitesResponse, videosResponse] = await Promise.all([
        fetch("/data/sites.json"),
        fetch("/data/videos.json"),
      ]);
      if (!sitesResponse.ok || !videosResponse.ok) {
        throw new Error("Map data could not be loaded.");
      }
      setSites(await sitesResponse.json());
      setVideos(await videosResponse.json());
    }

    loadData().catch((reason: Error) => setError(reason.message));
  }, []);

  const videosById = useMemo(() => new Map(videos.map((video) => [video.id, video])), [videos]);
  const selectedSite = sites.find((site) => site.id === selectedSiteId) ?? null;

  function siteMatches(site: Site) {
    if (videoOnly && site.videoIds.length === 0) {
      return false;
    }

    const normalizedQuery = normalize(query.trim());
    if (!normalizedQuery) {
      return true;
    }

    const haystack = [
      site.name,
      ...site.aliases,
      ...site.videoIds.map((id) => videosById.get(id)?.title ?? ""),
    ]
      .map(normalize)
      .join(" ");
    return haystack.includes(normalizedQuery);
  }

  const selectedVideos = selectedSite
    ? selectedSite.videoIds.map((id) => videosById.get(id)).filter((video): video is Video => Boolean(video))
    : [];
  const normalizedQuery = normalize(query.trim());
  const shouldShowAllLabels = zoom >= 1.65 || normalizedQuery.length > 0;

  function zoomBy(delta: number) {
    setZoom((current) => {
      const next = Math.min(4, Math.max(1, Number((current + delta).toFixed(2))));
      if (next === 1) {
        setPan({ x: 0, y: 0 });
      }
      return next;
    });
  }

  function resetView() {
    setZoom(1);
    setPan({ x: 0, y: 0 });
  }

  function handlePointerDown(event: PointerEvent<HTMLDivElement>) {
    if ((event.target as HTMLElement).closest(".site-label, .zoom-button")) {
      return;
    }
    pointersRef.current.set(event.pointerId, { x: event.clientX, y: event.clientY });
    if (pointersRef.current.size === 2) {
      const points = [...pointersRef.current.values()];
      pinchRef.current = {
        distance: Math.hypot(points[0].x - points[1].x, points[0].y - points[1].y),
        center: {
          x: (points[0].x + points[1].x) / 2,
          y: (points[0].y + points[1].y) / 2,
        },
        zoom,
        pan,
      };
      dragRef.current = null;
    } else {
      dragRef.current = { pointerId: event.pointerId, x: event.clientX, y: event.clientY };
    }
    event.currentTarget.setPointerCapture(event.pointerId);
  }

  function handlePointerMove(event: PointerEvent<HTMLDivElement>) {
    if (pointersRef.current.has(event.pointerId)) {
      pointersRef.current.set(event.pointerId, { x: event.clientX, y: event.clientY });
    }
    if (pinchRef.current && pointersRef.current.size >= 2) {
      const points = [...pointersRef.current.values()].slice(0, 2);
      const distance = Math.hypot(points[0].x - points[1].x, points[0].y - points[1].y);
      const center = {
        x: (points[0].x + points[1].x) / 2,
        y: (points[0].y + points[1].y) / 2,
      };
      const nextZoom = Math.min(4, Math.max(1, pinchRef.current.zoom * (distance / pinchRef.current.distance)));
      setZoom(Number(nextZoom.toFixed(2)));
      setPan({
        x: pinchRef.current.pan.x + center.x - pinchRef.current.center.x,
        y: pinchRef.current.pan.y + center.y - pinchRef.current.center.y,
      });
      return;
    }

    const drag = dragRef.current;
    if (!drag || drag.pointerId !== event.pointerId) {
      return;
    }
    const dx = event.clientX - drag.x;
    const dy = event.clientY - drag.y;
    dragRef.current = { ...drag, x: event.clientX, y: event.clientY };
    setPan((current) => ({ x: current.x + dx, y: current.y + dy }));
  }

  function handlePointerUp(event: PointerEvent<HTMLDivElement>) {
    pointersRef.current.delete(event.pointerId);
    if (pointersRef.current.size < 2) {
      pinchRef.current = null;
    }
    if (dragRef.current?.pointerId === event.pointerId) {
      dragRef.current = null;
    }
  }

  return (
    <main className="app">
      <section className="map-panel" aria-label="Interactive Neolithic map">
        <div className="map-toolbar">
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            type="search"
            placeholder="Search sites or videos"
            autoComplete="off"
            aria-label="Search sites or videos"
          />
          <label className="toggle">
            <input
              checked={videoOnly}
              onChange={(event) => setVideoOnly(event.target.checked)}
              type="checkbox"
            />
            <span>With videos</span>
          </label>
        </div>

        <div
          className="map-viewport"
          onPointerDown={handlePointerDown}
          onPointerMove={handlePointerMove}
          onPointerUp={handlePointerUp}
          onPointerCancel={handlePointerUp}
        >
          <div className="zoom-controls" aria-label="Map zoom controls">
            <button className="zoom-button" type="button" onClick={() => zoomBy(0.35)} aria-label="Zoom in">
              +
            </button>
            <button className="zoom-button" type="button" onClick={() => zoomBy(-0.35)} aria-label="Zoom out">
              -
            </button>
            <button className="zoom-button zoom-reset" type="button" onClick={resetView}>
              {Math.round(zoom * 100)}%
            </button>
          </div>
          <div
            className="map-wrap"
            style={{
              transform: `translate(${pan.x}px, ${pan.y}px) scale(${zoom})`,
            }}
          >
            <img
              src="/assets/fertile-crescent-neolithic-b.svg"
              alt="Fertile Crescent Neolithic B circa 7500 BC map"
              draggable={false}
            />
            <div className="markers" aria-live="polite">
              {sites.map((site) => {
                const isVisible = siteMatches(site);
                const firstVideo = site.videoIds
                  .map((id) => videosById.get(id))
                  .find((video): video is Video => Boolean(video));
                const showLabel = shouldShowAllLabels || selectedSiteId === site.id;
                return (
                  <div
                    key={site.id}
                    className={[
                      "site-anchor",
                      showLabel ? "show-label" : "",
                      site.videoIds.length ? "" : "no-video",
                      site.coordinateQuality === "estimated-region" ? "estimated" : "",
                      isVisible ? "" : "is-hidden",
                    ]
                      .filter(Boolean)
                      .join(" ")}
                    style={{
                      left: `${(site.x / SVG_WIDTH) * 100}%`,
                      top: `${(site.y / SVG_HEIGHT) * 100}%`,
                    }}
                  >
                    <span className="map-dot" aria-hidden="true" />
                    {firstVideo ? (
                      <a
                        className="site-label"
                        aria-current={site.id === selectedSiteId}
                        href={firstVideo.url}
                        rel="noopener noreferrer"
                        target="_blank"
                        title={`${site.name}: open ${firstVideo.title}`}
                        onClick={() => setSelectedSiteId(site.id)}
                      >
                        {site.name}
                      </a>
                    ) : (
                      <button
                        type="button"
                        className="site-label"
                        aria-current={site.id === selectedSiteId}
                        title={`${site.name}: no linked video yet`}
                        onClick={() => setSelectedSiteId(site.id)}
                      >
                        {site.name}
                      </button>
                    )}
                    {site.wikipediaUrl ? (
                      <a
                        className="wiki-link"
                        href={site.wikipediaUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        title={`${site.name}: open Wikipedia`}
                        aria-label={`${site.name}: open Wikipedia`}
                        onClick={() => setSelectedSiteId(site.id)}
                      >
                        <img src="https://en.wikipedia.org/static/favicon/wikipedia.ico" alt="" />
                      </a>
                    ) : null}
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </section>

      <aside className="detail-panel" aria-label="Site details">
        <div className="site-details">
          {error ? (
            <>
              <h1>Map failed to load</h1>
              <p>{error}</p>
            </>
          ) : selectedSite ? (
            <>
              <h1>{selectedSite.name}</h1>
              <div className="meta">
                <span>
                  SVG position: {selectedSite.x}, {selectedSite.y}
                </span>
                <span>Coordinate quality: {selectedSite.coordinateQuality ?? "unknown"}</span>
                <span>
                  {selectedVideos.length} linked video{selectedVideos.length === 1 ? "" : "s"}
                </span>
              </div>
              {selectedSite.wikipediaUrl ? (
                <a
                  className="detail-wiki-link"
                  href={selectedSite.wikipediaUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <img src="https://en.wikipedia.org/static/favicon/wikipedia.ico" alt="" />
                  Wikipedia
                </a>
              ) : null}
              {selectedVideos.length ? (
                <>
                {selectedVideos.length > 1 ? (
                  <a
                    className="primary-video-link"
                    href={selectedVideos[0].url}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    Open first video on YouTube
                  </a>
                ) : null}
                <div className="video-list">
                  {selectedVideos.map((video) => (
                    <a
                      className="video-link"
                      href={video.url}
                      key={video.id}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      {/* eslint-disable-next-line @next/next/no-img-element */}
                      <img src={video.thumbnail ?? ""} alt="" />
                      <span>
                        <span className="video-title">{video.title}</span>
                        <span className="video-duration">{video.durationString ?? ""}</span>
                      </span>
                    </a>
                  ))}
                </div>
                </>
              ) : (
                <p>No Ancient Architects video is linked to this site yet.</p>
              )}
            </>
          ) : (
            <>
              <h1>Neolithic Video Map</h1>
              <p>Zoom in or search to reveal site names, then tap a linked name to open its Ancient Architects video on YouTube.</p>
            </>
          )}
        </div>
        <div className="source-note">
          Base map: Wikimedia Commons, CC BY-SA 4.0. Video metadata: Ancient Architects
          YouTube playlists.
        </div>
      </aside>
    </main>
  );
}
