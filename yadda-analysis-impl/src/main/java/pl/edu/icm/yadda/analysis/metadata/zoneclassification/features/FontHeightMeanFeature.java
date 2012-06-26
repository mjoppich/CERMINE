package pl.edu.icm.yadda.analysis.metadata.zoneclassification.features;

import pl.edu.icm.yadda.analysis.classification.features.FeatureCalculator;
import pl.edu.icm.yadda.analysis.textr.model.BxChunk;
import pl.edu.icm.yadda.analysis.textr.model.BxLine;
import pl.edu.icm.yadda.analysis.textr.model.BxPage;
import pl.edu.icm.yadda.analysis.textr.model.BxWord;
import pl.edu.icm.yadda.analysis.textr.model.BxZone;

/** 
 * @author Pawel Szostek (p.szostek@icm.edu.pl) 
 */

public class FontHeightMeanFeature implements FeatureCalculator<BxZone, BxPage> {

    private static String featureName = "FontHeightMean";

    @Override
    public String getFeatureName() {
        return featureName;
    }

    @Override
    public double calculateFeatureValue(BxZone zone, BxPage page) {
    	Double heightSum = 0.0;
    	Integer heightNumber = 0;
    	for(BxLine line: zone.getLines())
    		for(BxWord word: line.getWords())
    			for(BxChunk chunk: word.getChunks()) {
    				heightSum += chunk.getBounds().getHeight();
    				++heightNumber;
    			}
    	return heightSum/heightNumber;
    }

}
