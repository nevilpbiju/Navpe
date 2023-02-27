
package com.example.navpe.Recognitions;

import java.util.List;

public interface ResultsView {
  void setResults(final List<SimilarityClassifier.Recognition> results);
}
