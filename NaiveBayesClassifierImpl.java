import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

/**
 * Your implementation of a naive bayes classifier. Please implement all four
 * methods.
 */

public class NaiveBayesClassifierImpl implements NaiveBayesClassifier {
	private Instance[] m_trainingData;
	// size of dictionary: total number of word types
	private int m_v;
	private double m_delta;
	public int m_sports_count, m_business_count;
	public int m_sports_word_count, m_business_word_count;
	private HashMap<String, Integer> m_map[] = new HashMap[2];

	/**
	 * Trains the classifier with the provided training data and vocabulary size
	 */
	@Override
	public void train(Instance[] trainingData, int v) {
		// TODO : Implement
		// For all the words in the documents, count the number of occurrences.
		// Save in HashMap
		// e.g.
		// m_map[0].get("catch") should return the number of "catch" es, in the
		// documents labeled sports
		// Hint: m_map[0].get("asdasd") would return null, when the word has not
		// appeared before.
		// Use m_map[0].put(word,1) to put the first count in.
		// Use m_map[0].replace(word, count+1) to update the value
		m_trainingData = trainingData;
		m_v = v;
		documents_per_label_count(m_trainingData);
		words_per_label_count(m_trainingData);
		// 0 for sports, 1 for not
		m_map[0] = new HashMap<>();
		m_map[1] = new HashMap<>();
		for (Instance doc : m_trainingData) {
			if (doc.label.equals(Label.SPORTS)) {
				for (String s : doc.words) {
					s = s.toLowerCase();
					if (m_map[0].get(s) != null) {
						int count_s = m_map[0].get(s);
						m_map[0].replace(s, count_s + 1);
					} else {
						m_map[0].put(s, 1);
					}
				}
			} else if (doc.label.equals(Label.BUSINESS)) {
				for (String s : doc.words) {
					s = s.toLowerCase();
					if (m_map[1].get(s) != null) {
						int count_b = m_map[1].get(s);
						m_map[1].replace(s, count_b + 1);
					} else {
						m_map[1].put(s, 1);
					}
				}
			} else {
				System.err.println("Label not match");
				System.exit(1);
			}
		}
	}

	/*
	 * Counts the number of documents for each label
	 */
	public void documents_per_label_count(Instance[] trainingData) {
		// TODO : Implement
		m_sports_count = 0;
		m_business_count = 0;
		for (Instance doc : trainingData) {
			if (doc.label.equals(Label.SPORTS)) {
				m_sports_count++;
			} else if (doc.label.equals(Label.BUSINESS)) {
				m_business_count++;
			}
		}
	}

	/*
	 * Prints the number of documents for each label
	 */
	public void print_documents_per_label_count() {
		System.out.println("SPORTS=" + m_sports_count);
		System.out.println("BUSINESS=" + m_business_count);
	}

	/*
	 * Counts the total number of words for each label
	 */
	public void words_per_label_count(Instance[] trainingData) {
		// TODO : Implement
		m_sports_word_count = 0; // C0(v)
		m_business_word_count = 0; // C1(v)
		for (Instance doc : trainingData) {
			if (doc.label.equals(Label.SPORTS)) {
				m_sports_word_count += doc.words.length;
			} else if (doc.label.equals(Label.BUSINESS)) {
				m_business_word_count += doc.words.length;
			}
		}
	}

	/*
	 * Prints out the number of words for each label
	 */
	public void print_words_per_label_count() {
		System.out.println("SPORTS=" + m_sports_word_count);
		System.out.println("BUSINESS=" + m_business_word_count);
	}

	/**
	 * Returns the prior probability of the label parameter, i.e. P(SPORTS) or
	 * P(BUSINESS)
	 */
	@Override
	public double p_l(Label label) {
		// TODO : Implement
		// Calculate the probability for the label. No smoothing here.
		// Just the number of label counts divided by the number of documents.
		double ret = 0;

		double sum = m_sports_count + m_business_count;

		if (label.equals(Label.SPORTS)) {
			ret = (double) m_sports_count / sum;
		} else if (label.equals(Label.BUSINESS)) {
			ret = (double) m_business_count / sum;
		}
		// else {
		// System.err.println("No such label exits");
		// System.exit(1);
		// }
		return ret;
	}

	/**
	 * Returns the smoothed conditional probability of the word given the label,
	 * i.e. P(word|SPORTS) or P(word|BUSINESS)
	 */
	@Override
	public double p_w_given_l(String word, Label label) {
		// TODO : Implement
		// Calculate the probability with Laplace smoothing for word in
		// class(label)
		double ret = 0;
		m_delta = 0.00001;
		double nmrtr = 0.0;
		double dnmrtr = 0.0;
		word = word.toLowerCase();
		if (label.equals(Label.SPORTS)) {
			if (m_map[0].get(word) == null) {
				nmrtr = m_delta;
			} else {
				nmrtr = (double) m_map[0].get(word) + m_delta;
			}
			dnmrtr = (double) (m_v * m_delta) + (double) m_sports_word_count;
		} else if (label.equals(Label.BUSINESS)) {
			if (m_map[1].get(word) == null) {
				nmrtr = m_delta;
			} else {
				nmrtr = (double) m_map[1].get(word) + m_delta;
			}
			dnmrtr = (double) (m_v * m_delta) + (double) m_business_word_count;
		}

		ret = (double) nmrtr / dnmrtr;
		return ret;
	}

	/**
	 * Classifies an array of words as either SPORTS or BUSINESS.
	 */
	@Override
	public ClassifyResult classify(String[] words) {
		// TODO : Implement
		// Sum up the log probabilities for each word in the input data, and the
		// probability of the label
		// Set the label to the class with larger log probability
		ClassifyResult ret = new ClassifyResult();
		ret.label = Label.SPORTS;
		ret.log_prob_sports = 0;
		ret.log_prob_business = 0;

		for (String s : words) {
			// System.out.print(Math.log(p_w_given_l(s,Label.SPORTS)));

			ret.log_prob_sports += Math.log(p_w_given_l(s, Label.SPORTS));
		}

		ret.log_prob_sports += Math.log(p_l(Label.SPORTS));
		// System.out.print(p_l(Label.SPORTS));//
		// System.exit(1);//
		for (String s : words) {
			ret.log_prob_business += Math.log(p_w_given_l(s, Label.BUSINESS));
		}
		ret.log_prob_business += Math.log(p_l(Label.BUSINESS));

		if (ret.log_prob_business > ret.log_prob_sports) {
			ret.label = Label.BUSINESS;
		}
		return ret;
	}

	/*
	 * Constructs the confusion matrix
	 */
	@Override
	public ConfusionMatrix calculate_confusion_matrix(Instance[] testData) {
		// TODO : Implement
		// Count the true positives, true negatives, false positives, false
		// negatives
		int TP, FP, FN, TN;
		TP = 0;
		FP = 0;
		FN = 0;
		TN = 0;

		for (Instance doc : testData) {
			ClassifyResult test = classify(doc.words);
			if (test.label.equals(Label.SPORTS)) {
				if (test.label.equals(doc.label)) {
					TP++;
				} else {
					FP++;
				}
			} else {
				if (test.label.equals(doc.label)) {
					TN++;
				} else {
					FN++;
				}
			}
		}
		return new ConfusionMatrix(TP, FP, FN, TN);
	}

}
