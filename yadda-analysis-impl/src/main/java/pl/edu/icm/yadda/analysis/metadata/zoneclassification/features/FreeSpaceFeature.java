package pl.edu.icm.yadda.analysis.metadata.zoneclassification.features;

import pl.edu.icm.yadda.analysis.classification.features.FeatureCalculator;
import pl.edu.icm.yadda.analysis.textr.model.BxLine;
import pl.edu.icm.yadda.analysis.textr.model.BxPage;
import pl.edu.icm.yadda.analysis.textr.model.BxWord;
import pl.edu.icm.yadda.analysis.textr.model.BxZone;
import pl.edu.icm.yadda.analysis.textr.model.BxChunk;

/** 
 * @author Pawel Szostek (p.szostek@icm.edu.pl) 
 */

public class FreeSpaceFeature implements FeatureCalculator<BxZone, BxPage> {

	private static String featureName = "FreeSpace";

	@Override
	public String getFeatureName() {
		return featureName;
	}

	@Override
	public double calculateFeatureValue(BxZone zone, BxPage page) {
		double charSpace = 0.0;

		for (BxLine line : zone.getLines()) {
			for (BxWord word : line.getWords())
				for (BxChunk chunk : word.getChunks()) {
					charSpace += chunk.getArea();
				}
		}
		return zone.getArea() - charSpace;
	}

}
