//////////////////////////////////////////////////////////////////
// Auxiliary.

function Point(x, y) {
    this.x = x;
    this.y = y;
}

function Rectangle(x, y, width, height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
}

function eventX(e) {
    return e.offsetX == undefined ? e.layerX : e.offsetX;
}
function eventY(e) {
    return e.offsetY == undefined ? e.layerY : e.offsetY;
}

//////////////////////////////////////////////////////////////////
// Images.

// An image and related properties. This may include low-resolution image.
function ImageRecord(highresName, width, height, lowresName, num) {
    this.highresName = highresName;
    this.naturalWidth = width;
    this.naturalHeight = height;
    this.lowresName = lowresName;
    if (lowresName != highresName) {
        this.lowresImage = new Image();
        this.lowresImage.addEventListener("load", function() { completeLowresLoading(num); }, false);
    }
    this.lowresLoaded = false;
    this.highresImage = new Image();
    this.highresImage.addEventListener("load", function() { completeHighresLoading(num); }, false);
    this.highresLoaded = false;
    this.scale = 0.0;
    this.center = new Point(0.5, 0.5);
}
ImageRecord.prototype.load =
function() {
    if (this.lowresName != this.highresName) {
        this.lowresImage.src = this.lowresName;
    }
    this.highresImage.src = this.highresName;
};

// Records of images.
var imageRecords = [];

// Number between 0 and imageRecords.length. Or negative if none.
var currentImNum = -1;
// Increase of size upon zooming.
var zoomFactor = 1.2;
// Portion of window moved by buttons.
var navigatePortion = 0.2;

function isHighresImage() {
    return zoomCanvas && 0 <= currentImNum && currentImNum < imageRecords.length &&
        isCurrentHighresLoaded();
}
function isLowresImage() {
    return zoomCanvas && 0 <= currentImNum && currentImNum < imageRecords.length &&
        isCurrentLowresLoaded();
}
function isImage() {
    return isHighresImage() || isLowresImage();
}

function currentNatWidth() {
    return imageRecords[currentImNum].naturalWidth;
}
function currentNatHeight() {
    return imageRecords[currentImNum].naturalHeight;
}
function currentHighresImage() {
    return imageRecords[currentImNum].highresImage;
}
function currentLowresImage() {
    return imageRecords[currentImNum].lowresImage;
}
function isCurrentHighresLoaded() {
    return imageRecords[currentImNum].highresLoaded;
}
function isCurrentLowresLoaded() {
    return imageRecords[currentImNum].lowresLoaded;
}
function currentScale() {
    return imageRecords[currentImNum].scale;
}
function setCurrentScale(z) {
    imageRecords[currentImNum].scale = z;
}
function currentCenter() {
    return imageRecords[currentImNum].center;
}
function setCurrentCenter(x, y) {
    imageRecords[currentImNum].center = new Point(x, y);
}

function addImage(name, w, h, lowresName) {
    var num = imageRecords.length;
    var imageRecord = new ImageRecord(name, w, h, lowresName, num);
    imageRecords.push(imageRecord);
    imageRecord.load();
}

function completeHighresLoading(i) {
    imageRecords[i].highresLoaded = true;
    if (i == 0 && currentImNum < 0) {
        currentImNum = 0;
        adjustZoom();
        redrawCanvas();
        showNavigation();
    }
}
function completeLowresLoading(i) {
    imageRecords[i].lowresLoaded = true;
    if (i == 0 && currentImNum < 0) {
        currentImNum = 0;
        adjustZoom();
        redrawCanvas();
        showNavigation();
    }
}

function prevImage() {
    if (currentImNum-1 >= 0) {
        currentImNum--;
        adjustZoom();
        redrawCanvas();
        showNavigation();
    }
}
function nextImage() {
    if (currentImNum+1 < imageRecords.length) {
        currentImNum++;
        adjustZoom();
        redrawCanvas();
        showNavigation();
    }
}

function zoomIn() {
    if (isImage()) {
        setCurrentScale(currentScale() * zoomFactor);
        adjustZoom();
        redrawCanvas();
    }
}
function zoomInAroundMouse(x, y) {
    if (isImage()) {
        var xDiff = (zoomCanvas.offsetWidth / 2.0 - x) / currentScale();
        var yDiff = (zoomCanvas.offsetHeight / 2.0 - y) / currentScale();
        setCurrentScale(currentScale() * zoomFactor);
        var newCenterX = currentCenter().x - 
                (zoomFactor-1) * xDiff / currentNatWidth();
        var newCenterY = currentCenter().y - 
                (zoomFactor-1) * yDiff / currentNatHeight();
        setCurrentCenter(newCenterX, newCenterY);
        adjustZoom();
        redrawCanvas();
    }
}
function zoomOut() {
    if (isImage()) {
        setCurrentScale(currentScale() / zoomFactor);
        adjustZoom();
        redrawCanvas();
    }
}

function moveUp() {
    if (isImage()) 
        move(0, -zoomCanvas.offsetHeight * navigatePortion / currentScale() / currentNatHeight());
}
function moveDown() {
    if (isImage()) 
        move(0, zoomCanvas.offsetHeight * navigatePortion / currentScale() / currentNatHeight());
}
function moveLeft() {
    if (isImage()) 
        move(-zoomCanvas.offsetWidth * navigatePortion / currentScale() / currentNatWidth(), 0);
}
function moveRight() {
    if (isImage()) 
        move(zoomCanvas.offsetWidth * navigatePortion / currentScale() / currentNatWidth(), 0);
}
function move(xFactor, yFactor) {
    setCurrentCenter(currentCenter().x + xFactor, currentCenter().y + yFactor);
    adjustZoom();
    redrawCanvas();
}

function adjustZoom() {
    if (isImage()) {
        if (currentScale() * currentNatWidth() < zoomCanvas.offsetWidth  &&
                currentScale() * currentNatHeight() < zoomCanvas.offsetHeight) {
            setCurrentScale(Math.min(
                1.0 * zoomCanvas.offsetWidth / currentNatWidth(),
                1.0 * zoomCanvas.offsetHeight / currentNatHeight()));
        } else if (currentScale() > currentNatWidth() / 10 ||
                currentScale() > currentNatHeight() / 10) {
            setCurrentScale(Math.min(currentNatWidth() / 10, currentNatHeight() / 10));
        }
        adjustPos();
    }
}

function adjustPos() {
    var w = zoomCanvas.offsetWidth / currentScale() / currentNatWidth();
    var h = zoomCanvas.offsetHeight / currentScale() / currentNatHeight();
    var newCenterX = 0.5
    var newCenterY = 0.5
    if (w <= 1)  {
        newCenterX = Math.max(currentCenter().x, w / 2);
        newCenterX = Math.min(newCenterX, 1 - w / 2);
    } 
    if (h <= 1)  {
        newCenterY = Math.max(currentCenter().y, h / 2);
        newCenterY = Math.min(newCenterY, 1 - h / 2);
    } 
    setCurrentCenter(newCenterX, newCenterY);
}

//////////////////////////////////////////////////////////////////
// Canvas.

var zoomCanvas;

function initCanvas() {
    zoomCanvas = document.getElementById("zoom_canvas");
    if (zoomContext() == null)
        alert("You seem to have an outdated browser: " +
        navigator.userAgent + 
        " Full functionality is not available.");
    else
        resizeCanvas();
}

function zoomContext() {
    if (zoomCanvas) 
        return zoomCanvas.getContext("2d");
    else
        return null;
}

function redrawCanvas() {
    if (isImage()) {
        var isLowres = !isHighresImage();
        var ctx = zoomContext();
        ctx.fillStyle = "black";
        ctx.fillRect(0, 0, zoomCanvas.width, zoomCanvas.height);
        if (isLowres) {
            var image = currentLowresImage();
            var xScale = currentScale() * currentNatWidth() / image.naturalWidth;
            var yScale = currentScale() * currentNatHeight() / image.naturalHeight;
            var rect = visibleRectOfImage(image, xScale, yScale);
        } else {
            var image = currentHighresImage();
            var xScale = currentScale();
            var yScale = currentScale();
            var rect = visibleRect();
        }
        var sx = rect.x;
        var sy = rect.y;
        var sw = rect.width;
        var sh = rect.height;
        var x = 0; 
        var y = 0; 
        var w = zoomCanvas.offsetWidth;
        var h = zoomCanvas.offsetHeight;
        if (sx < 0 || sw > image.naturalWidth) {
            x = Math.round(-sx * xScale);
            w = zoomCanvas.offsetWidth - 2 * x;
            sx = Math.max(0, sx);
            sw = Math.min(image.naturalWidth, sw);
        } else if (sx + sw > image.naturalWidth)
            sw = image.naturalWidth - sx;
        if (sy < 0 || sh > image.naturalHeight) {
            y = Math.round(-sy * yScale);
            h = zoomCanvas.offsetHeight - 2 * y;
            sy = Math.max(0, sy);
            sh = Math.min(image.naturalHeight, sh);
        } else if (sy + sh > image.naturalHeight)
            sh = image.naturalHeight - sy;
        ctx.drawImage(image, sx, sy, sw, sh, x, y, w, h);
        drawAreas(ctx);
    } else if (zoomCanvas) {
        var ctx = zoomContext();
        ctx.fillStyle = "white";
        ctx.fillText("Loading images", 
                        Math.max(0, zoomCanvas.width / 2 - 50), 
                        Math.max(0, zoomCanvas.height / 2 - 20));
    }
}

// What rectangle should be visible?
function visibleRect() {
    var x = Math.round(currentCenter().x * currentNatWidth() 
                        - zoomCanvas.width / 2.0 / currentScale());
    var y = Math.round(currentCenter().y * currentNatHeight() 
                        - zoomCanvas.height / 2.0 / currentScale());
    var w = Math.round(zoomCanvas.width / currentScale());
    var h = Math.round(zoomCanvas.height / currentScale());
    if (w < 1 || h < 1)
        return new Rectangle(0, 0, 0, 0);
    else
        return new Rectangle(x, y, w, h);
}
// Same, but for image (which can be low-resolution) and scaling factors.
function visibleRectOfImage(image, xScale, yScale) {
    var x = Math.round(currentCenter().x * image.naturalWidth 
                        - zoomCanvas.width / 2.0 / xScale);
    var y = Math.round(currentCenter().y * image.naturalHeight 
                        - zoomCanvas.height / 2.0 / yScale);
    var w = Math.round(zoomCanvas.width / xScale);
    var h = Math.round(zoomCanvas.height / yScale);
    if (w < 1 || h < 1)
        return new Rectangle(0, 0, 0, 0);
    else
        return new Rectangle(x, y, w, h);
}

function resizeCanvas() {
    if (zoomCanvas) {
        var col = document.getElementById("left");
        if (col != null) {
            zoomCanvas.width = col.offsetWidth;
            zoomCanvas.height = col.offsetHeight;
        }
        adjustZoom();
        redrawCanvas();
    }
}

//////////////////////////////////////////////////////////////////
// Font.

var fontsDone = false;

// Check whether fonts have loaded. If not, wait and try anew.
function waitForFonts(f, c) {
    if (fontsDone)
        f();
    else if (fontsLoaded() == "") {
        fontsDone = true;
        f();
    } else if (c > 40) {
        // alert("seems unable to load fonts" + fontsLoaded());
        alert("seem unable to load fonts; perhaps try again later?");
    } else {
        setTimeout(function(){ waitForFonts(f, c+1); }, 1000);
    }
}

//////////////////////////////////////////////////////////////////
// Hieroglyphic.

// Total margin on left and right together.
var hiFormatMargin = 60;

// Make canvas for hieroglyphic.
function makeHiSometime(elem, breaking) {
    waitForFonts(function(){ makeHi(elem, breaking); }, 0);
}
function makeHi(elem, breaking) {
    removeHi(elem);
    var his = elem.getElementsByTagName("canvas");
    for (var i = 0; i < his.length; i++) {
        var hi = his[i];
        if (hi.className == "res") {
            var ctx = hi.getContext("2d");
            ctx.setTransform(1, 0, 0, 1, 0, 0);
            ctx.clearRect(0, 0, hi.width, hi.height);
            if (hi.hasChildNodes() && hi.firstChild.nodeType == 3) {
                var code = hi.firstChild.nodeValue;
                var resLite = parseResLite(code);
                var resContext = new ResContext();
                if (breaking) {
                    var w = document.getElementById("left").offsetWidth - hiFormatMargin;
                    var div = makeDivision(resLite, w, resContext, false);
                } else 
                    var div = makeDivision(resLite, Number.MAX_VALUE, resContext, false);
                div.render(hi);
                if (breaking && div.remainder.groups != null) 
                    makeMoreHi(hi, div.remainder);
            }
        }
    }
}
// Add what didn't fit in first canvas.
function makeMoreHi(previous, resLite) {
    var resContext = new ResContext();
    var w = document.getElementById("left").offsetWidth - hiFormatMargin;
    var div = makeDivision(resLite, w, resContext, false);
    var hi = document.createElement("canvas");
    hi.className = "res_extra";
    if (previous.nextSibling != null)
        previous.parentNode.insertBefore(hi, previous.nextSibling);
    else
        previous.parentNode.appendChild(hi);
    div.render(hi);
    if (div.initialNumber > 0 && div.remainder.groups != null) 
        makeMoreHi(hi, div.remainder);
}

// Remove extra hieroglyphic from page.
// (The extra while loop is needed on Chrome.)
function removeHi(elem) {
    var done = true;
    while (done) {
        done = false;
        var his = elem.getElementsByTagName("canvas");
        for (var i = 0; i < his.length; i++) {
            var hi = his[i];
            if (hi.className == "res_extra") {
                hi.parentNode.removeChild(hi);
                done = true;
            }
        }
    }
}

// Reformat all hieroglyphic on current page.
function reformatHiero() {
    var pages = document.getElementsByTagName("div");
    for (var i = 0; i < pages.length; i++) {
        var page = pages[i];
        if (page.className == "shown_text_page") 
            makeHiSometime(page, true);
    }
}

// Avoid doing reformat too often.
var reformatTimer = null;
function reformatHieroUponTimeout() {
    if (reformatTimer)
        clearTimeout(reformatTimer);
    reformatTimer = setTimeout(function(){ reformatHiero(); }, 500);
}

//////////////////////////////////////////////////////////////////
// Image navigation.

// Navigation buttons only shown briefly after mouse move.
var navTimer = null;

function initNavigation() {
    var prev = document.getElementById("prev");
    var next = document.getElementById("next");
    if (prev) {
        prev.className = "hidden";
        prev.addEventListener("click", navPrev);
    }
    if (next) {
        next.className = "hidden";
        next.addEventListener("click", navNext);
    }
}

function navPrev(e) {
    e.preventDefault();
    prevImage();
}
function navNext(e) {
    e.preventDefault();
    nextImage();
}

function showNavigation() {
    if (navTimer) 
        clearTimeout(navTimer);
    makeNavigation(true);
    navTimer = setTimeout(function(){ makeNavigation(false); }, 1500);
}

function makeNavigation(show) {
    var prev = document.getElementById("prev");
    if (prev) {
        if (show && currentImNum > 0) 
            prev.className = "shown";
        else 
            prev.className = "hidden";
    }
    var next = document.getElementById("next");
    if (next) {
        if (show && currentImNum >= 0 && currentImNum < imageRecords.length-1) 
            next.className = "shown";
        else
            next.className = "hidden";
    }
}

//////////////////////////////////////////////////////////////////
// Areas.

// A (cubic) Bezier is given by four points.
function Curve(fromx, fromy, cntx1, cnty1, cntx2, cnty2, tox, toy) {
    this.fromx = fromx;
    this.fromy = fromy;
    this.cntx1 = cntx1;
    this.cnty1 = cnty1;
    this.cntx2 = cntx2;
    this.cnty2 = cnty2;
    this.tox = tox;
    this.toy = toy;
}
Curve.prototype.transformed =
function() {
    var rect = visibleRect();
    return new Curve(
                (this.fromx - rect.x) * currentScale(),
                (this.fromy - rect.y ) * currentScale(),
                (this.cntx1 - rect.x) * currentScale(),
                (this.cnty1 - rect.y) * currentScale(),
                (this.cntx2 - rect.x) * currentScale(),
                (this.cnty2 - rect.y) * currentScale(),
                (this.tox - rect.x) * currentScale(),
                (this.toy - rect.y) * currentScale());
};

// An area is in an image, and has a number of Bezier curves.
// It may also be referenced from text with id.
// When an area is dashed, we can have varying thicknesses.
// Areas get an index, which an increasing number;
// when clicked, areas with lower index get priority.
function Area(imNum, dashSize, curves, index) {
    this.imNum = imNum;
    this.dashSize = dashSize;
    this.curves = curves;
    this.id = null;
    this.index = index;
}
Area.prototype.rectangle =
function() {
    if (this.curves.length == 0)
        return new Rectangle(0, 0, 0, 0);
    var minX = Number.MAX_VALUE;
    var maxX = -Number.MAX_VALUE;
    var minY = Number.MAX_VALUE ;
    var maxY = -Number.MAX_VALUE;
    for (var i = 0; i < this.curves.length; i++) {
        var curve = this.curves[i];
        minX = Math.min(minX, curve.fromx);
        maxX = Math.max(maxX, curve.fromx);
        minY = Math.min(minY, curve.fromy);
        maxY = Math.max(maxY, curve.fromy);
    }
    return new Rectangle(minX, minY, maxX-minX, maxY-minY);
};

// All areas, identified by tag.
var areas = [];
// Number of areas.
var nAreas = 0;
// Text id mapped to area tags.
var idAreaTags = []
// Area tags per page.
var pageAreaTags = [];
// Tags of areas that have focus.
var focusAreaTags = [];

// Tags of areas on page.
function currentAreaTags() {
    var currents = [];
    var tags = pageAreaTags[currentPage];
    if (tags != null) 
        for (var i = 0; i < tags.length; i++) {
            var tag = tags[i];
            var area = areas[tag];
            if (area != null && area.imNum == currentImNum) 
                currents.push(tag);
        }
    return currents;
}

// Get tags of areas with focus.
function setFocusAreaTags(id) {
    focusAreaTags = [];
    var tags = idAreaTags[id];
    if (tags != null)
        for (var i = 0; i < tags.length; i++) {
            var tag = tags[i];
            var area = areas[tag];
            if (area != null && area.id == id)
                focusAreaTags.push(tag);
        }
}

// An area is delimited by curves. Initially there are none.
function addArea(imNum, tag, dashSize) {
    areas[tag] = new Area(imNum, dashSize, [], (nAreas++));
    
}
// Add curve to existing area.
function addCurve(tag, fromx, fromy, cntx1, cnty1, cntx2, cnty2, tox, toy) {
    var area = areas[tag];
    if (area) 
        area.curves.push(
                new Curve(fromx, fromy, cntx1, cnty1, cntx2, cnty2, tox, toy));
}
// Add link between area (tag) and text (id).
function addTagId(tag, id) {
    var area = areas[tag];
    if (area != null) {
        area.id = id;
        if (idAreaTags[id] == null)
            idAreaTags[id] = [];
        idAreaTags[id].push(tag);
    }
}

// Add text reference directly to page reference.
function connectIdToAreas(id, page) {
    if (pageAreaTags[page] == null)
        pageAreaTags[page] = [];
    if (idAreaTags[id] != null) {
        var tags = idAreaTags[id];
        if (tags != null)
            for (var i = 0; i < tags.length; i++) 
                pageAreaTags[page].push(tags[i]);
    }
}

function drawAreas(ctx) {
    var tags = currentAreaTags();
    for (var i = 0; i < tags.length; i++) {
        var tag = tags[i];
        var area = areas[tag];
        if (focusAreaTags.indexOf(tag) >= 0) {
            ctx.lineWidth = 8;
            if (ctx.setLineDash)
                ctx.setLineDash([1,0]);
            else  {
                ctx.mozDash = [1,0];
                ctx.webkitLineDash = [1,0];
            }
            ctx.strokeStyle = "rgba(0,0,255,0.4)";
        } else {
            ctx.lineWidth = area.dashSize;
            if (ctx.setLineDash) {
                ctx.setLineDash([5,2]);
            } else {
                ctx.mozDash = [5,2];
                ctx.webkitLineDash = [5,2];
            }
            ctx.strokeStyle = "rgba(0,0,255,0.4)";
        }
        for (var j = 0; area != null && j < area.curves.length; j++) {
            var curve = area.curves[j].transformed();
            if (j == 0) {
                ctx.beginPath();
                ctx.moveTo(curve.fromx, curve.fromy);
            }
            ctx.bezierCurveTo(curve.cntx1, curve.cnty1, curve.cntx2, curve.cnty2, 
                            curve.tox, curve.toy);
        }
        ctx.closePath();
        ctx.stroke();
    }
}

function checkInsidePath(x, y, ctx) {
    var tags = currentAreaTags();
    var bestArea = null;
    var minIndex = Number.MAX_VALUE;
    for (var i = 0; i < tags.length; i++) {
        var tag = tags[i];
        var area = areas[tag];
        for (var j = 0; area != null && j < area.curves.length; j++) {
            var curve = area.curves[j].transformed();
            if (j == 0) {
                ctx.beginPath();
                ctx.moveTo(curve.fromx, curve.fromy);
            }
            ctx.bezierCurveTo(curve.cntx1, curve.cnty1, curve.cntx2, curve.cnty2, 
                            curve.tox, curve.toy);
        }
        ctx.closePath();
        if (ctx.isPointInPath(x, y) && area.id != null && area.index < minIndex) {
            minIndex = area.index;
            bestArea = area;
        }
    }
    if (bestArea != null)
        focusTextById(bestArea.id);
}

function insidePath(x, y, ctx) {
    var tags = currentAreaTags();
    for (var i = 0; i < tags.length; i++) {
        var tag = tags[i];
        var area = areas[tag];
        for (var j = 0; area != null && j < area.curves.length; j++) {
            var curve = area.curves[j].transformed();
            if (j == 0) {
                ctx.beginPath();
                ctx.moveTo(curve.fromx, curve.fromy);
            }
            ctx.bezierCurveTo(curve.cntx1, curve.cnty1, curve.cntx2, curve.cnty2, 
                            curve.tox, curve.toy);
        }
        ctx.closePath();
        if (ctx.isPointInPath(x, y) && area.id != null) 
            return true;
    }
    return false;
}

// Center on first area for ref.
function centerArea() {
    for (var i = 0; i < focusAreaTags.length; i++) {
        var tag = focusAreaTags[i];
        var area = areas[tag];
        if (area != null && area.imNum == currentImNum) {
            centerAreaTag(tag);
            return;
        }
    }
    redrawCanvas();
}
function centerAreaTag(tag) {
    var area = areas[tag];
    if (area == null) {
        redrawCanvas();
        return;
    }
    var rect = area.rectangle();
    var vis = visibleRect();
    var rectMaxX = rect.x + rect.width;
    var rectMaxY = rect.y + rect.height;
    var visMaxX = vis.x + vis.width;
    var visMaxY = vis.y + vis.height;
    if (vis.x <= rectMaxX && rect.x <= visMaxX &&
            vis.y <= rectMaxY && rect.y <= visMaxY) {
        redrawCanvas();
        return;
    }
    var xDiff = (rect.x + rect.width / 2.0) - (vis.x + vis.width / 2.0);
    var yDiff = (rect.y + rect.height / 2.0) - (vis.y + vis.height / 2.0);
    move(xDiff / currentNatWidth(), yDiff/ currentNatHeight());
}

//////////////////////////////////////////////////////////////////
// Tabs.

// Page of tab.
var currentPage = null;

// Set up classes and listeners connected to tabs.
function initTextTabs() {
    var as = document.getElementsByTagName("a");
    var pageNum = 0;
    for (var i = 0; i < as.length; i++) {
        var a = as[i];
        if (a.className == "text_tab") {
            a.addEventListener("click", selectTextTab);
            var ref = a.getAttribute("href");
            if (ref == null) 
                continue;
            var page = document.getElementById(ref);
            if (page == null) 
                continue;
            if (pageNum++ == 0) {
                a.parentNode.className = "active";
                page.className = "shown_text_page";
                currentPage = ref;
            } else {
                a.parentNode.className = "inactive";
                page.className = "hidden_text_page";
            }
            var divs = page.getElementsByTagName("div");
            for (var j = 0; j < divs.length; j++) {
                var div = divs[j];
                if (div.className == "focusable") {
                    var id = div.getAttribute("id");
                    if (id != null)
                        connectIdToAreas(id, ref);
                    div.addEventListener("click", focusText);
                }
            }
        }
    }
}

// Triggered by click on tab.
function selectTextTab(e) {
    e.preventDefault();
    var ref = this.getAttribute("href");
    var pages = document.getElementsByTagName("div");
    for (var i = 0; i < pages.length; i++) {
        var page = pages[i];
        if (page.id != ref && page.className == "shown_text_page") 
            page.className = "hidden_text_page";
        else if (page.id == ref && page.className == "hidden_text_page") {
            page.className = "shown_text_page";
            makeHiSometime(page, true);
        }
    }
    currentPage = ref;
    this.parentNode.className = "active";
    var as = document.getElementsByTagName("a");
    for (var i = 0; i < as.length; i++) {
        var a = as[i];
        if (a.className == "text_tab" && a.getAttribute("href") != ref)
            a.parentNode.className = "inactive";
    }
    redrawCanvas();
}

// Triggered by click on text within page.
function focusText(e) {
    var divs = document.getElementsByTagName("div");
    for (var i = 0; i < divs.length; i++) {
        var div = divs[i];
        if (div.className == "focus" && div != this) 
            div.className = "focusable";
        else if (div == this)
            div.className = "focus";
    }
    var id = this.getAttribute("id");
    setFocusAreaTags(id);
    centerArea();
}

// Triggered by click on area.
function focusTextById(id) {
    var divs = document.getElementsByTagName("div");
    for (var i = 0; i < divs.length; i++) {
        var div = divs[i];
        var divId = div.getAttribute("id");
        if (div.className == "focus" && divId != id) 
            div.className = "focusable";
        else if (divId == id) {
            div.className = "focus";
            div.scrollIntoView(false);
        }
    }
    setFocusAreaTags(id);
    redrawCanvas();
}

//////////////////////////////////////////////////////////////////
// Popup.

// Set up classes and listeners connected to popup.
function initPopup() {
    setPopup("hidden");
    var as = document.getElementsByTagName("a");
    for (var i = 0; i < as.length; i++) {
        var a = as[i];
        if (a.className == "popupref" ||
                a.className == "popuparrow") 
            a.addEventListener("click", makePopup);
    }
    var popupClose = document.getElementById("popupclose");
    if (popupClose != null)
        popupClose.addEventListener("click", closePopup);
}

// Link to popup is clicked. Show it.
function makePopup(e) {
    e.preventDefault();
    var id = this.getAttribute("href");
    var divs = document.getElementsByTagName("div");
    for (var i = 0; i < divs.length; i++) {
        var div = divs[i];
        var divId = div.getAttribute("id");
        if (div.className == "shown_popup" && divId != id) 
            div.className = "hidden_popup";
        else if (divId == id) {
            div.className = "shown_popup";
            makeHiSometime(div, false);
        }
    }
    setPopup("shown");
}

function closePopup(e) {
    e.preventDefault();
    setPopup("hidden");
}

function setPopup(stat) {
    var popup = document.getElementById("popup");
    if (popup != null)
        popup.className = stat;
}

//////////////////////////////////////////////////////////////////
// Peripherals.

function processKey(e) {
    e.preventDefault();
    switch (e.keyCode) {
        case 33: prevImage(); break; // page up
        case 34: nextImage(); break; // page down
        case 37: moveLeft(); break; // left
        case 38: moveUp(); break; // up
        case 39: moveRight(); break; // right
        case 40: moveDown(); break; // down
        case 188: zoomOut(); break; // <
        case 190: zoomIn(); break; // >
    }
}

function doMousewheel(e) {
    e.preventDefault();
    var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail)));
    if (delta > 0 && zoomCanvas) 
        zoomInAroundMouse(e.pageX - zoomCanvas.offsetLeft, 
            e.pageY - zoomCanvas.offsetTop);
    else 
        zoomOut();
}

// Mouse position before dragging.
var dragStart = null;
var dragged = false

function doMousedown(e) {
    e.preventDefault();
    dragStart = new Point(eventX(e), eventY(e));
    dragged = false;
    zoomCanvas.style.cursor = "move";
}
function doMouseup(e) {
    e.preventDefault();
    if (!dragged && isImage()) 
        checkInsidePath(eventX(e), eventY(e), zoomContext());
    dragStart = null;
    dragged = false;
    zoomCanvas.style.cursor = "default";
}
function doMouseout(e) {
    e.preventDefault();
    dragStart = null;
    dragged = false;
    zoomCanvas.style.cursor = "default";
}
function doMousemove(e) {
    e.preventDefault();
    if (dragStart) {
        var delta = 5;
        var xDiff = eventX(e)-dragStart.x;
        var yDiff = eventY(e)-dragStart.y;
        if (Math.abs(xDiff) > delta || Math.abs(yDiff) > delta) {
            xDiff = xDiff / currentScale() / currentNatWidth();
            yDiff = yDiff / currentScale() / currentNatHeight();
            move(-xDiff, -yDiff);
            dragStart = new Point(eventX(e), eventY(e));
            dragged = true;
            redrawCanvas();
        }
    } else 
        zoomCanvas.style.cursor = insidePath(eventX(e), eventY(e), zoomContext()) ? 
                "pointer" : "default";
    showNavigation();
}

function initPeripherals() {
    document.addEventListener("keydown", processKey);
    if (zoomCanvas) {
        zoomCanvas.addEventListener("mousewheel", doMousewheel);
        zoomCanvas.addEventListener("DOMMouseScroll", doMousewheel);
        zoomCanvas.addEventListener("mousedown", doMousedown);
        zoomCanvas.addEventListener("mouseup", doMouseup);
        zoomCanvas.addEventListener("mouseout", doMouseout);
        zoomCanvas.addEventListener("mousemove", doMousemove);
    }
}

//////////////////////////////////////////////////////////////////
// Initialization.

function init() {
    initTextTabs();
    initPopup();
    initCanvas();
    initNavigation();
    initPeripherals();
}

function refresh() {
    resizeCanvas();
    reformatHieroUponTimeout();
}

window.addEventListener("DOMContentLoaded", init);
window.addEventListener("resize", refresh);
