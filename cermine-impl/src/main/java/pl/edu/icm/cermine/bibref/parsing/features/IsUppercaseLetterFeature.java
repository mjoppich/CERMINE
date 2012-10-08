package pl.edu.icm.cermine.bibref.parsing.features;

import pl.edu.icm.cermine.bibref.parsing.model.Citation;
import pl.edu.icm.cermine.bibref.parsing.model.CitationToken;
import pl.edu.icm.cermine.tools.classification.features.FeatureCalculator;

/**
 *
 * @author Dominika Tkaczyk (dtkaczyk@icm.edu.pl)
 */
public class IsUppercaseLetterFeature extends FeatureCalculator<CitationToken, Citation> {

    @Override
    public double calculateFeatureValue(CitationToken object, Citation context) {
        return (object.getText().matches("^[A-Z]$")) ? 1 : 0;
    }

}
