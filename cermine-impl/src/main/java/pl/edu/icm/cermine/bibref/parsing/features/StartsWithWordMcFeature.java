package pl.edu.icm.cermine.bibref.parsing.features;

import pl.edu.icm.cermine.bibref.parsing.model.Citation;
import pl.edu.icm.cermine.bibref.parsing.model.CitationToken;
import pl.edu.icm.cermine.tools.classification.features.FeatureCalculator;

/**
 *
 * @author Dominika Tkaczyk (dtkaczyk@icm.edu.pl)
 */
public class StartsWithWordMcFeature extends FeatureCalculator<CitationToken, Citation> {

    @Override
    public double calculateFeatureValue(CitationToken object, Citation context) {
        return (object.getText().toLowerCase().startsWith("mc")) ? 1 : 0;
    }

}
