---
license: cc-by-4.0
task_categories:
- translation
- text-classification
- zero-shot-classification
size_categories:
- 10K<n<100K
tags:
- sumerian
---


# SumTablets 🏺  A Transliteration Dataset of Sumerian Tablets

Preprocessing scripts on [GitHub](https://github.com/colesimmons/SumTablets). We welcome contributions!

---

# What is it?

SumTablets is a dataset of 91,606 Sumerian cuneiform tablets, structured as glyph--transliteration pairs. It is designed for the task of Sumerian transliteration, a conventional system for rendering an interpretation of a tablet using the Latin alphabet. We build upon existing data with the aim of providing:

1. **Structure**. The transliterations provided by other projects are heavily annotated for a variety of symbolic search and analysis tasks. However, to be best suited for use with modern language models, it is best to strip this away so that the resulting transliteration best represents just what is present on a tablet. Moreover, given a transliteration, it is not obvious how to use it for any interesting task. So to that end, we use dictionaries to map each reading back into its corresponding cuneiform glyph, represented in Unicode. The result is a set of parallel examples, designed for modeling transliteration as a sequence-to-sequence task.

2. **Accessibility**. How can we make it as easy as possible for people to contribute to this problem? By designing/structuring the dataset for this task, we aim to take care of all of the data sourcing and preprocessing steps as a barrier to get started training models. By publishing on Hugging Face, we aim to make the dataset discoverable.
   
3. **Reproducibility**. There are two factors that govern the end state of a dataset: (1) the source data and (2) the steps taken during preprocessing. To ensure the reproducibility of any experiments built on top of this data, we intend to use versioning to reflect when either of these change.

---

# Publication

**📝 Forthcoming**: To be presented at the inaugural [ML4AL Workshop](https://www.ml4al.com/), ACL 2024

**👨‍💻 Authors**: [Cole Simmons](https://github.com/colesimmons), [Richard Diehl Martinez](https://github.com/rdiehlmartinez), and Prof. Dan Jurafsky

---

# Acknowledgements

For nearly thirty years, Assyriologists have been manually typing and publishing transliterations online. These efforts began in 1996 with the [Electronic Text Corpus of Sumerian Literature (ETCSL)](https://etcsl.orinst.ox.ac.uk/#), which became archival in 2006. It was soon followed by the [Cuneiform Digital Library Initiative (CDLI)](https://cdli.mpiwg-berlin.mpg.de/), the [Open Richly Annotated Cuneiform Corpus (Oracc)](https://oracc.museum.upenn.edu/), and others. Our work in particular pulls from the [Electronic Pennsylvania Sumerian Dictionary (ePSD2)](https://oracc.museum.upenn.edu/epsd2/index.html), which aggregates data from across the various Oracc projects. These projects have embraced the collaborative spirit and freely shared their data with each other and with the public.

This dataset is only possible thanks to the hard work and generosity of contributors to all of these projects. To continue the tradition, we release this dataset with a CC-BY license.

---

# Versions

- **v1** (2024-05-12): initial release

---

# What's in it?

## Composition (Period)

| Period | Train | Val | Test |
|:----------------------|:-------|:-----|:------|
| Ur III | 71,116 | 3,951 | 3,951 |
| Old Akkadian | 4,766 | 265 | 265 |
| Early Dynastic IIIb | 3,467 | 192 | 192 |
| Old Babylonian | 1,374 | 73 | 73 |
| Lagash II | 788 | 44 | 44 |
| Early Dynastic IIIa | 755 | 42 | 42 |
| Early Dynastic I-II | 77 | 4 | 4 |
| Unknown | 68 | 4 | 4 |
| Neo-Assyrian | 20 | 1 | 1 |
| Neo-Babylonian | 14 | 1 | 1 |
| Middle Babylonian | 7 | 0 | 0 |
| **Total** | 82,452 | 4,577 | 4,577 |

## Composition (Genre)

| Genre | Train | Val | Test |
|:----------------|:-------|:-----|:------|
| Administrative | 77,193 | 4,259 | 4,291 |
| Royal Inscription | 2,611 | 151 | 146 |
| Literary | 1,000 | 63 | 62 |
| Letter | 718 | 48 | 33 |
| Legal | 544 | 35 | 36 |
| Unknown | 269 | 14 | 7 |
| Lexical | 69 | 0 | 0 |
| Liturgy | 40 | 4 | 1 |
| Math/Science | 8 | 3 | 1 |
| **Total** | 82,452 | 4,577 | 4,577 |


<!-- https://github.com/huggingface/huggingface_hub/blob/main/src/huggingface_hub/templates/datasetcard_template.md?plain=1) -->