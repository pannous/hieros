const SVG_WIDTH = 2421.2596;
const SVG_HEIGHT = 2480.3192;

const state = {
  sites: [],
  videos: [],
  selectedSiteId: null,
  query: "",
  videoOnly: false,
};

const markersEl = document.querySelector("#markers");
const detailsEl = document.querySelector("#siteDetails");
const searchEl = document.querySelector("#search");
const videoOnlyEl = document.querySelector("#videoOnly");

function normalize(value) {
  return value
    .normalize("NFKD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase();
}

function videoById(id) {
  return state.videos.find((video) => video.id === id);
}

function siteMatches(site) {
  const query = normalize(state.query.trim());
  if (state.videoOnly && site.videoIds.length === 0) {
    return false;
  }
  if (!query) {
    return true;
  }

  const haystack = [
    site.name,
    ...(site.aliases || []),
    ...site.videoIds.map((id) => videoById(id)?.title || ""),
  ]
    .map(normalize)
    .join(" ");
  return haystack.includes(query);
}

function renderMarkers() {
  markersEl.innerHTML = "";

  for (const site of state.sites) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = [
      "marker",
      site.videoIds.length ? "" : "no-video",
      site.coordinateQuality === "estimated-region" ? "estimated" : "",
      siteMatches(site) ? "" : "is-hidden",
    ]
      .filter(Boolean)
      .join(" ");
    button.style.left = `${(site.x / SVG_WIDTH) * 100}%`;
    button.style.top = `${(site.y / SVG_HEIGHT) * 100}%`;
    button.setAttribute("aria-label", site.name);
    button.setAttribute(
      "title",
      `${site.name}${site.videoIds.length ? ` (${site.videoIds.length} videos)` : ""}`,
    );
    button.setAttribute("aria-current", String(site.id === state.selectedSiteId));
    button.addEventListener("click", () => selectSite(site.id));
    markersEl.appendChild(button);
  }
}

function selectSite(siteId) {
  state.selectedSiteId = siteId;
  const site = state.sites.find((candidate) => candidate.id === siteId);
  renderMarkers();
  renderDetails(site);
}

function renderDetails(site) {
  if (!site) {
    detailsEl.innerHTML = `
      <h1>Neolithic Video Map</h1>
      <p>Select a site marker to see Ancient Architects videos linked to that place.</p>
    `;
    return;
  }

  const videos = site.videoIds.map(videoById).filter(Boolean);
  const videoHtml = videos.length
    ? `<div class="video-list">${videos
        .map(
          (video) => `
            <a class="video-link" href="${video.url}" target="_blank" rel="noopener noreferrer">
              <img src="${video.thumbnail || ""}" alt="">
              <span>
                <span class="video-title">${video.title}</span>
                <span class="video-duration">${video.durationString || ""}</span>
              </span>
            </a>
          `,
        )
        .join("")}</div>`
    : "<p>No Ancient Architects video is linked to this site yet.</p>";

  detailsEl.innerHTML = `
    <h2>${site.name}</h2>
    <div class="meta">
      <span>SVG position: ${site.x}, ${site.y}</span>
      <span>Coordinate quality: ${site.coordinateQuality || "unknown"}</span>
      <span>${videos.length} linked video${videos.length === 1 ? "" : "s"}</span>
    </div>
    ${videoHtml}
  `;
}

async function loadData() {
  const [sitesResponse, videosResponse] = await Promise.all([
    fetch("./data/sites.json"),
    fetch("./data/videos.json"),
  ]);
  state.sites = await sitesResponse.json();
  state.videos = await videosResponse.json();
  renderMarkers();
}

searchEl.addEventListener("input", (event) => {
  state.query = event.target.value;
  renderMarkers();
});

videoOnlyEl.addEventListener("change", (event) => {
  state.videoOnly = event.target.checked;
  renderMarkers();
});

loadData().catch((error) => {
  detailsEl.innerHTML = `<h1>Map failed to load</h1><p>${error.message}</p>`;
});
