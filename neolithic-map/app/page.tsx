"use client";

import { type KeyboardEvent, type PointerEvent, type WheelEvent, useEffect, useMemo, useRef, useState } from "react";

const SVG_WIDTH = 2421.2596;
const SVG_HEIGHT = 2480.3192;

type Video = {
  id: string;
  title: string;
  url: string;
  durationString?: string | null;
  thumbnail?: string | null;
  channel?: string | null;
  publishedAt?: string | null;
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

function formatDate(value?: string | null) {
  return value
    ? new Intl.DateTimeFormat("en", {
        year: "numeric",
        month: "short",
        day: "numeric",
      }).format(new Date(value))
    : null;
}

function toYouTubeQuery(value: string) {
  return encodeURIComponent(value).replace(/%20/g, "+");
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
    ? selectedSite.videoIds
        .map((id) => videosById.get(id))
        .filter((video): video is Video => Boolean(video))
    : [];
  const normalizedQuery = normalize(query.trim());
  const shouldShowAllLabels = zoom >= 1.35 || normalizedQuery.length > 0 || selectedSiteId !== null;

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

  function handleWheel(event: WheelEvent<HTMLDivElement>) {
    event.preventDefault();
    const bounds = event.currentTarget.getBoundingClientRect();
    const pointer = {
      x: event.clientX - bounds.left,
      y: event.clientY - bounds.top,
    };

    const zoomFactor = Math.exp(-event.deltaY * 0.0015);
    const nextZoom = Math.min(4, Math.max(1, Number((zoom * zoomFactor).toFixed(2))));

    if (nextZoom === zoom) {
      return;
    }

    const focalPoint = {
      x: (pointer.x - pan.x) / zoom,
      y: (pointer.y - pan.y) / zoom,
    };

    setPan(
      nextZoom === 1
        ? { x: 0, y: 0 }
        : {
            x: pointer.x - focalPoint.x * nextZoom,
            y: pointer.y - focalPoint.y * nextZoom,
          },
    );
    setZoom(nextZoom);
  }

  function handlePointerDown(event: PointerEvent<HTMLDivElement>) {
    if ((event.target as Element).closest(".site-anchor, .zoom-button")) {
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

  function handleSiteKeyDown(event: KeyboardEvent<SVGGElement>, siteId: string) {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      setSelectedSiteId(siteId);
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
          onWheel={handleWheel}
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
            <svg
              className="markers"
              viewBox={`0 0 ${SVG_WIDTH} ${SVG_HEIGHT}`}
              preserveAspectRatio="xMidYMid meet"
              aria-label="Neolithic sites"
            >
              {sites.map((site) => {
                const isVisible = siteMatches(site);
                const firstVideo = site.videoIds
                  .map((id) => videosById.get(id))
                  .find((video): video is Video => Boolean(video));
                const showLabel = shouldShowAllLabels || selectedSiteId === site.id;
                const classes = [
                  "site-anchor",
                  site.videoIds.length ? "" : "no-video",
                  site.coordinateQuality === "estimated-region" ? "estimated" : "",
                  isVisible ? "" : "is-hidden",
                ]
                  .filter(Boolean)
                  .join(" ");

                return (
                  <g
                    key={site.id}
                    className={classes}
                    role="button"
                    tabIndex={0}
                    aria-label={`Select ${site.name}`}
                    aria-current={site.id === selectedSiteId}
                    onClick={() => setSelectedSiteId(site.id)}
                    onKeyDown={(event) => handleSiteKeyDown(event, site.id)}
                  >
                    <title>
                      {[
                        site.name,
                        Number.isFinite((site as Site & { latitude?: number }).latitude) &&
                        Number.isFinite((site as Site & { longitude?: number }).longitude)
                          ? `WGS84: ${(site as Site & { latitude?: number }).latitude}, ${(site as Site & { longitude?: number }).longitude}`
                          : null,
                      ]
                        .filter(Boolean)
                        .join("\n")}
                    </title>
                    <circle className="map-dot" cx={site.x} cy={site.y} r="6" />
                    <text
                      className={`site-label${showLabel ? "" : " is-label-hidden"}`}
                      x={site.x + 12 / zoom}
                      y={site.y + 9 / zoom}
                      style={{
                        fontSize: `${38 / zoom}px`,
                        strokeWidth: 8 / zoom,
                      }}
                    >
                      {site.name}
                    </text>
                  </g>
                );
              })}
            </svg>
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
              <div className="detail-actions">
                <a
                  className="detail-wiki-link"
                  href={`https://duckduckgo.com/?q=${encodeURIComponent(
                    `${selectedSite.name} archaeological site`,
                  )}`}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Search Web
                </a>
                <a
                  className="detail-search-link"
                  href={`https://www.youtube.com/results?search_query=${toYouTubeQuery(selectedSite.name)}`}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Search YouTube
                </a>
              </div>
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
                        <img src={video.thumbnail ?? ""} alt="" />
                        <span>
                          <span className="video-title">{video.title}</span>
                          <span className="video-meta">
                            {video.durationString ? <span>{video.durationString}</span> : null}
                            {video.publishedAt ? <span>{formatDate(video.publishedAt)}</span> : null}
                          </span>
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
              <p>Zoom in to reveal city names, then select one to open its Ancient Architects video or inspect the site.</p>
            </>
          )}
        </div>
        <details className="source-note">
          <summary>Sources</summary>
          <p>Base map: Wikimedia Commons, CC BY-SA 4.0. Video metadata: Ancient Architects YouTube playlists.</p>
        </details>
      </aside>
    </main>
  );
}
