package nederhof.ocr.hiero;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;

import nederhof.ocr.images.*;
import nederhof.ocr.images.distance.*;
import nederhof.ocr.admin.*;
import nederhof.ocr.guessing.*;
import nederhof.ocr.hiero.admin.*;
import nederhof.util.*;
import nederhof.util.collections.*;
import nederhof.util.math.*;
import nederhof.util.xml.*;

// Guesser of glyphs.
public class HieroGuesser extends OcrGuesser {

	// Make guesser for directory of prototypes.
	public HieroGuesser(String loc) throws IOException {
		super(loc);
	}
	public HieroGuesser(File dir) throws IOException {
		super(dir);
	}

    // Beam for FFT.
    public void setBeam(int b) {
        this.fftBeam = b;
    }
    public int getBeam() {
        return this.fftBeam;
    }
    // Beam for final selection.
    public void setCandidates(int b) {
        this.distBeam = b;
    }
    public int getCandidates() {
        return this.distBeam;
    }

    ////////////////////////////////////////////////////////
    // Index file.

	private GlyphStats stats;

    // Read index file (if exists) with information on handwriting.
    protected void readIndexFile() throws IOException {
        stats = new ConnectedGlyphStats(dir);
		makeHeight();
		makePreferred();
    }

    private class ConnectedGlyphStats extends GlyphStats {
        public ConnectedGlyphStats(File dir) throws IOException {
            super(dir);
        }
    }

	// Mapping from signs to normal heights, relative to unit size.
	protected GlyphHeight glyphHeight;

	// Make height and add information from index file.
	private void makeHeight() {
		glyphHeight = new GlyphHeight();
		for (Map.Entry<String, LogNormalDistribution> entry : stats.getNameToSize().entrySet()) {
			glyphHeight.put(entry.getKey(), (float) entry.getValue().getMean());
		}
	}

    // In order to prevent rare signs from being proposed 
    // ahead of more likely signs, ignore certain signs.
    // These are later proposed as second alternatives.
    protected TreeMap<String,Vector<String>> followedBy;
    // Those that are not guessed at first instance.
    protected Collection<String> forbidden;

	// Get preferred names from index file.
	private void makePreferred() {
		followedBy = new TreeMap<String,Vector<String>>();
		forbidden = new TreeSet<String>();
		for (NamePair pair : stats.getPreferNames()) {
			String more = pair.first;
			String less = pair.second;
            if (followedBy.get(more) == null)
                followedBy.put(more, new Vector<String>());
            followedBy.get(more).add(less);
            forbidden.add(less);
		}
	}

	protected boolean isAllowed(String name) {
		return !forbidden.contains(name);
	}

	// Add extra names.
	protected Vector<WeightedElem<String>> extend(Vector<WeightedElem<String>> weightedNames) {
		Vector<WeightedElem<String>> extended = new Vector<WeightedElem<String>>();
		TreeSet<String> seen = new TreeSet<String>();
		for (WeightedElem<String> weightedName : weightedNames) {
			String name = weightedName.elem();
			float weight = weightedName.weight();
			if (!seen.contains(name)) {
				extended.add(weightedName);
				seen.add(name);
				if (followedBy.containsKey(name))
					for (String follower : followedBy.get(name)) {
						extended.add(new WeightedElem<String>(follower, weight));
						seen.add(follower);
					}
			}
		}
		return extended;
	}

	//////////////////////////////////////////////////////////
	// Models.

	protected DimensionCost dimCost;
	protected FreqCost freqCost;
	protected SizeCost sizeCost;
	protected RatioCost ratioCost;
	protected FftCost fftCost;
	protected DistortionModel distModel;

    protected void initModels(int nImagesMax) {
        dimCost = new DimensionCost(nImagesMax);
        freqCost = new FreqCost(nImagesMax);
        sizeCost = new SizeCost(nImagesMax);
        ratioCost = new RatioCost(nImagesMax);
        fftCost = new FftCost(nImagesMax);
        distModel = new IDM();
    }

    protected void addToModels(File file, String fileName, String glyphName) 
			throws IOException {
        BinaryImage im = new BinaryImage(file);
        float h = glyphHeight.containsKey(glyphName) ? glyphHeight.get(glyphName) : -1;
		freqCost.add((float) stats.getRelFreq(glyphName));
		sizeCost.add(stats.getSize(glyphName));
		ratioCost.add(stats.getRatio(glyphName));
        dimCost.addImage(im, h);
        fftCost.addImage(im);
    }

	protected String getGlyphName(String fileName) {
		return fileName.replaceAll("-.*", "");
	}

	protected String getNoGlyphName() {
		return "\"?\"";
	}

    ////////////////////////////////////////////////////////
    // Finding best prototypes.

	public Vector<String> findNBestNames(BinaryImage im, float height, int distBeam) {
		float margin = 1.05f;
		long startTime = System.nanoTime();
		Vector<WeightedElem<Integer>> candidates = fullSearch();
		candidates = fftSearch(im, candidates, fftBeam);
		candidates = distSearch(im, candidates, distBeam);
		Vector<WeightedElem<String>> candidatesStr = weightedNamesOfWeightedCandidates(candidates);
		candidatesStr = extend(candidatesStr);
		Vector<String> candidatesUnweighted = tieBreaking(candidatesStr, margin, im, height);
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);
		/*
		System.out.println("(2) took " + (duration / 1000));
				for (int i : best)
					System.out.println("(2) " + i);
		*/
		return candidatesUnweighted;
	}

	protected Vector<String> rerank(Vector<String> candidates, BinaryImage im, float height) {
		Vector<WeightedElem<String>> weightedNames = fullSearchNames(candidates);
		weightedNames = freqSearchNames(weightedNames, Integer.MAX_VALUE);
		weightedNames = sizeSearchNames(height, weightedNames, Integer.MAX_VALUE);
		weightedNames = ratioSearchNames(im, weightedNames, Integer.MAX_VALUE);
		candidates = unweightedNames(weightedNames);
		return candidates;
	}

	// Take selection of prototypes, according to dimension model.
    protected Vector<WeightedElem<Integer>> dimSearch(BinaryImage im, float height, 
            Vector<WeightedElem<Integer>> candidates, int beam) {
        float[] values = dimCost.getValues(im, height);
        BeamQueue<Integer> queue = new BeamQueue<Integer>(beam);
        for (WeightedElem<Integer> weightedElem : candidates) {
            int elem = weightedElem.elem();
            float weight = weightedElem.weight();
            float cost = dimCost.getCost(values, elem);
            float sum = cost;
            queue.add(elem, sum);
        }
        return queue.bestWeighted();
    }
	// As above, but do not use beam, or comparison between weights.
	protected Vector<WeightedElem<Integer>> dimSearchNoBeam(BinaryImage im, float height,
			Vector<WeightedElem<Integer>> candidates, int beam) {
		float[] values = dimCost.getValues(im, height);
		Vector<WeightedElem<Integer>> selection = new Vector<WeightedElem<Integer>>();
		for (WeightedElem<Integer> weightedElem : candidates) {
			int elem = weightedElem.elem();
			float weight = weightedElem.weight();
			float cost = dimCost.getCost(values, elem);
			if (cost < 0.5) {
				float sum = cost;
				selection.add(new WeightedElem<Integer>(elem, sum));
			}
		}
		return selection;
	}

	// Take selection of prototypes, according to FFT model.
	protected Vector<WeightedElem<Integer>> fftSearch(BinaryImage im, 
			Vector<WeightedElem<Integer>> candidates, int beam) {
		float[] values = fftCost.getValues(im);
		BeamQueue<Integer> queue = new BeamQueue<Integer>(beam);
		for (WeightedElem<Integer> weightedElem : candidates) {
			int elem = weightedElem.elem();
			float weight = weightedElem.weight();
			float cost = fftCost.getCost(values, elem);
			float sum = cost;
			queue.add(elem, sum);
		}
		return queue.bestWeighted();
	}

	// Take selection of prototypes, according to frequency, with names.
	protected Vector<WeightedElem<String>> freqSearchNames(Vector<WeightedElem<String>> names, int beam) {
		BeamQueue<String> queue = new BeamQueue<String>(beam);
		for (WeightedElem<String> weightedName : names) {
			String name = weightedName.elem();
			float weight = weightedName.weight();
			double p = stats.getRelFreq(name);
			float cost = (float) NegLogProb.to(p);
			float sum = weight + cost;
			queue.add(name, sum);
		}
		return queue.bestWeighted();
	}

    // Take selection of prototypes, according to size model.
	protected Vector<WeightedElem<Integer>> sizeSearch(float height,
			Vector<WeightedElem<Integer>> candidates, int beam) {
		float value = sizeCost.getValue(height);
		BeamQueue<Integer> queue = new BeamQueue<Integer>(beam);
		for (WeightedElem<Integer> weightedElem : candidates) {
			int elem = weightedElem.elem();
			float weight = weightedElem.weight();
			float cost = sizeCost.getCost(value, elem);
			if (cost < Float.MAX_VALUE) {
				float sum = cost;
				queue.add(elem, sum);
			}
		}
		return queue.bestWeighted();
	}
	// As above, but with names.
	protected Vector<WeightedElem<String>> sizeSearchNames(float height,
			Vector<WeightedElem<String>> names, int beam) {
		float value = sizeCost.getValue(height);
		BeamQueue<String> queue = new BeamQueue<String>(beam);
		for (WeightedElem<String> weightedName : names) {
			String name = weightedName.elem();
			float weight = weightedName.weight();
			LogNormalDistribution normal = stats.getSize(name);
			float density = (float) normal.density(value);
			if (density < Float.MIN_VALUE) {
				density = Float.MIN_VALUE * 10;
			}
			float cost = (float) NegLogProb.to(density);
			float sum = weight + cost;
			queue.add(name, sum);
		}
		return queue.bestWeighted();
	}

    // Take selection of prototypes, according to ratio model.
	protected Vector<WeightedElem<Integer>> ratioSearch(BinaryImage im, 
			Vector<WeightedElem<Integer>> candidates, int beam) {
		float value = ratioCost.getValue(im);
		BeamQueue<Integer> queue = new BeamQueue<Integer>(beam);
		for (WeightedElem<Integer> weightedElem : candidates) {
			int elem = weightedElem.elem();
			float weight = weightedElem.weight();
			float cost = ratioCost.getCost(value, elem);
			if (cost < Float.MAX_VALUE) {
				float sum = cost;
				queue.add(elem, sum);
			}
		}
		return queue.bestWeighted();
	}
	// As above, but with names.
	protected Vector<WeightedElem<String>> ratioSearchNames(BinaryImage im, 
			Vector<WeightedElem<String>> names, int beam) {
		float value = ratioCost.getValue(im);
		BeamQueue<String> queue = new BeamQueue<String>(beam);
		for (WeightedElem<String> weightedName : names) {
			String name = weightedName.elem();
			float weight = weightedName.weight();
			LogNormalDistribution normal = stats.getRatio(name);
			float density = (float) normal.density(value);
			if (density < Float.MIN_VALUE) {
				density = Float.MIN_VALUE * 10;
			}
			float cost = (float) NegLogProb.to(density);
			float sum = weight + cost;
			queue.add(name, sum);
		}
		return queue.bestWeighted();
	}

    // Take selection of prototypes, according to distortion model.
    protected Vector<WeightedElem<Integer>> distSearch(BinaryImage im,
            Vector<WeightedElem<Integer>> candidates, int beam) {
        BeamQueue<Integer> queue = new BeamQueue<Integer>(beam);
        try {
            for (WeightedElem<Integer> weightedElem : candidates) {
                int elem = weightedElem.elem();
                float weight = weightedElem.weight();
                BufferedImage proto = ImageIO.read(records[elem].file);
                float cost = distModel.distort(im, proto);
                float sum = cost;
                queue.add(elem, sum);
            }
        } catch (IOException e) {
            // ignore
        }
        return queue.bestWeighted();
    }

	public float cost(BinaryImage im, BufferedImage proto) {
        return distModel.distort(im, proto);
    }

    ///////////////////////////////////////////////////////
    // For testing.

    public static void main(String[] args) {
        try {
            HieroGuesser guesser = new HieroGuesser(args[0]);
            File dir = new File(args[0]);
            File[] files = dir.listFiles();
            for (File f : files) {
                if (f.isFile() && FileAux.hasExtension(f.getName(), "png")) {
                    String name = FileAux.removeExtension(f.getName(), "png");
                    if (name.matches(".*-.*"))
                        continue;
                    String gardName = name.replaceAll("-.*", "");
                    System.out.println(gardName + " " + name);
					/*
                    try {
                        BinaryImage im = new BinaryImage(f);
                        Vector<Integer> best = guesser.dimAndFftSearch(im, 0.7f, 5);
                        if (best.size() > 0)
                            System.out.println(guesser.records[best.get(0)].fileName);
                        best = guesser.distSearchOld(im, best, 5);
                        if (best.size() > 0)
                            System.out.println(guesser.records[best.get(0)].fileName);
                    } catch (NullPointerException e) {
                        // ignore
                    }
					*/
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
