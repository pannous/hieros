"use client";

import { useEffect, useMemo, useState } from "react";

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

        <div className="map-wrap">
          <img
            src="/assets/fertile-crescent-neolithic-b.svg"
            alt="Fertile Crescent Neolithic B circa 7500 BC map"
          />
          <div className="markers" aria-live="polite">
            {sites.map((site) => {
              const isVisible = siteMatches(site);
              return (
                <button
                  key={site.id}
                  type="button"
                  className={[
                    "marker",
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
                  aria-label={site.name}
                  aria-current={site.id === selectedSiteId}
                  title={`${site.name}${site.videoIds.length ? ` (${site.videoIds.length} videos)` : ""}`}
                  onClick={() => setSelectedSiteId(site.id)}
                />
              );
            })}
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
              {selectedVideos.length ? (
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
              ) : (
                <p>No Ancient Architects video is linked to this site yet.</p>
              )}
            </>
          ) : (
            <>
              <h1>Neolithic Video Map</h1>
              <p>Select a site marker to see Ancient Architects videos linked to that place.</p>
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
