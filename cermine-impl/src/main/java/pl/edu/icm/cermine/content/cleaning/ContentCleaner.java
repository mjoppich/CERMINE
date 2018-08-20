/**
 * This file is part of CERMINE project.
 * Copyright (c) 2011-2018 ICM-UW
 *
 * CERMINE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CERMINE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with CERMINE. If not, see <http://www.gnu.org/licenses/>.
 */

package pl.edu.icm.cermine.content.cleaning;

import java.util.ArrayList;
import java.util.List;
import pl.edu.icm.cermine.content.model.BxContentStructure;
import pl.edu.icm.cermine.content.model.BxContentStructure.BxDocContentPart;
import pl.edu.icm.cermine.structure.model.BxChunk;
import pl.edu.icm.cermine.structure.model.BxLine;
import pl.edu.icm.cermine.structure.model.BxWord;

/**
 * @author Dominika Tkaczyk (d.tkaczyk@icm.edu.pl)
 */
public class ContentCleaner {
    
    public static final double DEFAULT_PAR_LINE_MULT = 0.5;
    
    public static final double DEFAULT_MIN_PAR_IND = 5;
    
    public static final double DEFAULT_LAST_PAR_LINE_MULT = 0.8;
    
    public static final double DEFAULT_FIRST_PAR_LINE_SCORE = 3;
    
      
    private double paragraphLineIndentMultiplier = DEFAULT_PAR_LINE_MULT;
    
    private double minParagraphIndent = DEFAULT_MIN_PAR_IND;
    
    private double lastParagraphLineLengthMult = DEFAULT_LAST_PAR_LINE_MULT;
    
    private double firstParagraphLineMinScore = DEFAULT_FIRST_PAR_LINE_SCORE;
    
    public void cleanupContent(BxContentStructure contentStructure) {
        for (BxDocContentPart contentPart : contentStructure.getParts()) {
            List<BxLine> headerLines = contentPart.getHeaderLines();
            StringBuilder sb = new StringBuilder();
            for (BxLine headerLine : headerLines) {
                String lineText = headerLine.toText();
                if (lineText.endsWith("-")) {
                    lineText = lineText.substring(0, lineText.length()-1);
                    if (lineText.lastIndexOf(' ') < 0) {
                        sb.append(lineText);
                    } else {
                        sb.append(lineText.substring(0, lineText.lastIndexOf(' ')));
                        sb.append(" ");
                        sb.append(lineText.substring(lineText.lastIndexOf(' ')+1));
                    }
                } else {
                    sb.append(lineText);
                    sb.append(" ");
                }
            }
            contentPart.setCleanHeaderText(cleanLigatures(sb.toString().trim()));
            
            List<BxLine> contentLines = contentPart.getContentLines();
            List<String> contentTexts = new ArrayList<String>();

            ArrayList<ArrayList<BxLine>> contentTextLines = new ArrayList<ArrayList<BxLine>>();
            ArrayList<ArrayList<BxWord>> contentTextWords = new ArrayList<ArrayList<BxWord>>();


            double maxLen = Double.NEGATIVE_INFINITY;
            for (BxLine line : contentLines) {
                if (line.getWidth() > maxLen) {
                    maxLen = line.getWidth();
                }
            }
            
            String contentText = "";
            ArrayList<BxLine> textLines = new ArrayList<BxLine>();
            ArrayList<BxWord> lineWords = new ArrayList<BxWord>();
            boolean bDashRemoved = false;

            for (BxLine line : contentLines) {
                int score = 0;
                BxLine prev = line.getPrev();
                BxLine next = line.getNext();
                if (line.toText().matches("^[A-Z].*$")) {
                    score++;
                }
                if (prev != null) {
                    if (line.getX() > prev.getX() && line.getX() - prev.getX() < paragraphLineIndentMultiplier * maxLen 
                            && line.getX() - prev.getX() > minParagraphIndent) {
                        score++;
                    }
                    if (prev.getWidth() < lastParagraphLineLengthMult * maxLen) {
                        score++;
                    }
                    if (prev.toText().endsWith(".")) {
                        score++;
                    }
                }
                if (next != null && line.getX() > next.getX() && line.getX() - next.getX() < paragraphLineIndentMultiplier * maxLen 
                            && line.getX() - next.getX() > minParagraphIndent) {
                    score++;
                }
                
                if (score >= firstParagraphLineMinScore) {
                    if (!contentText.isEmpty()) {
                        contentTexts.add(cleanLigatures(contentText.trim()));
                        contentTextLines.add(textLines);

                        // get last BxWord
                        BxWord oLWord = lineWords.get(lineWords.size()-1);
                        oLWord.setText(oLWord.toText().trim());

                        for (BxWord oWord: lineWords)
                        {
                            oWord.setText(cleanLigatures(oWord.toText()));
                        }

                        contentTextWords.add(lineWords);


                    }
                    contentText = "";
                    bDashRemoved = false;
                    textLines = new ArrayList<BxLine>();
                    lineWords = new ArrayList<BxWord>();
                }
                
                String lineText = line.toText();
                ArrayList<BxWord> tlineWords = new ArrayList<>();
                for (BxWord oWord : line)
                {
                    oWord.setPage( contentPart.getPage().getId() );
                    tlineWords.add(oWord);
                }

                textLines.add(line);

                if (lineText.endsWith("-")) {
                    lineText = lineText.substring(0, lineText.length()-1);

                    BxWord oLWord = tlineWords.get(tlineWords.size()-1);
                    String oLWordT = oLWord.toText();

                    if (oLWordT.endsWith("-"))
                    {
                        oLWordT = oLWordT.substring(0, oLWordT.length()-1);
                        oLWord.setText(oLWordT);
                        oLWord.sConjunction = "";
                    }



                    if (lineText.lastIndexOf(' ') < 0) {

                        // there is no blank in this line, therefore we can take everything!

                        contentText += lineText;
                        // all words are taken
                        lineWords.addAll(tlineWords);

                    } else {

                        // there is a blank in this line
                        // we must find the blank and split the word (if within a word)

                        contentText += lineText.substring(0, lineText.lastIndexOf(' '));
                        contentText += " ";
                        contentText += lineText.substring(lineText.lastIndexOf(' ')+1);

                        BxWord oLastWordWithBlank = null;

                        for (BxWord oWord: tlineWords)
                        {
                            if (oWord.toText().contains(" "))
                            {
                                oLastWordWithBlank = oWord;
                            }
                        }

                        // add all words up to last word with blank

                        for (BxWord oWord: tlineWords)
                        {
                            if (oWord == oLastWordWithBlank)
                            {
                                break;
                            }

                            lineWords.add(oWord);
                        }


                        if (oLastWordWithBlank != null)
                        {

                            List<BxChunk> allChunks = new ArrayList<BxChunk>();
                            for (BxChunk oChunk: oLastWordWithBlank)
                            {
                                allChunks.add(oChunk);
                            }

                            // split last word
                            BxWord oFirstPart = new BxWord();
                            oFirstPart.setChunks(allChunks);
                            oFirstPart.toText();
                            oFirstPart.setBounds(oLastWordWithBlank.getBounds());

                            BxWord oSecondPart = new BxWord();
                            oSecondPart.setChunks(allChunks);
                            oSecondPart.toText();
                            oSecondPart.setBounds(oLastWordWithBlank.getBounds());

                            String sPrefixPart = oLastWordWithBlank.toText().substring(0, lineText.lastIndexOf(' '));
                            String sSuffixPart = oLastWordWithBlank.toText().substring(lineText.lastIndexOf(' ')+1);

                            oFirstPart.setText(sPrefixPart);
                            oSecondPart.setText(sSuffixPart);

                            oFirstPart.sConjunction = "";

                            lineWords.add(oFirstPart);
                            lineWords.add(oSecondPart);


                            // add remaining words
                            int iIdx = tlineWords.indexOf(oLastWordWithBlank);

                            for (int i = iIdx+1; i < tlineWords.size(); ++i)
                            {
                                lineWords.add(tlineWords.get(i));
                            }

                        }


                    }

                } else {

                    contentText += lineText + " ";
                    lineWords.addAll(tlineWords);

                    //contentText += "\n";
                }
            }

            if (!contentText.isEmpty()) {
                contentTexts.add(cleanLigatures(contentText.trim()));
                contentTextLines.add(textLines);

                // get last BxWord
                BxWord oLWord = lineWords.get(lineWords.size()-1);

                String sTrimmed =oLWord.toText().trim();

                if (sTrimmed.length() != oLWord.toText().length())
                {
                    oLWord.setText(sTrimmed);
                }

                for (BxWord oWord: lineWords)
                {
                    oWord.setText(cleanLigatures(oWord.toText()));
                }

                contentTextWords.add(lineWords);

            }
            
            contentPart.setCleanContentTexts(contentTexts);
            contentPart.setCleanContentLines(contentTextLines);
            contentPart.setCleanContentWords(contentTextWords);
        }
    }
    
    public static String cleanOther(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("[’‘]", "'")
                  .replaceAll("[–]", "-")  // EN DASH \u2013
                  .replaceAll("[—]", "-"); // EM DASH \u2014
    }
    
    public static String cleanLigatures(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("\uFB00", "ff")
                  .replaceAll("\uFB01", "fi")
                  .replaceAll("\uFB02", "fl")
                  .replaceAll("\uFB03", "ffi")
                  .replaceAll("\uFB04", "ffl")
                  .replaceAll("\uFB05", "ft")
                  .replaceAll("\uFB06", "st")
                  .replaceAll("\u00E6", "ae")
                  .replaceAll("\u0153", "oe");
    }
    
    public static String clean(String str) {
        if (str == null) {
            return null;
        }
        return cleanOther(cleanLigatures(str));
    }
    
    public static String cleanHyphenationAndBreaks(String str) {
        if (str == null) {
            return null;
        }
        return cleanHyphenation(str).replaceAll("\n", " ");
    }
    
    public static String cleanHyphenation(String str) {
        if (str == null) {
            return null;
        }
        str = str.replaceAll(" +", " ").replaceAll("^ +", "").replaceAll(" +$", "");
        String hyphenList = "\u002D\u00AD\u2010\u2011\u2012\u2013\u2014\u2015\u207B\u208B\u2212-";
        String[] lines = str.split("\n");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].replaceAll("^ +", "").replaceAll(" +$", "");
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < lines.length) {
            String line = lines[i];
            if (i + 1 == lines.length) {
                sb.append(line);
                break;
            }
            String next = lines[i+1];
            if (line.matches("^.*["+hyphenList+"]$")) {
                line = line.substring(0, line.length()-1);
                sb.append(line);
                int idx = next.indexOf(' ');
                if (idx < 0) {
                    sb.append(next);
                    i++;
                } else {
                    sb.append(next.substring(0, idx));
                    lines[i+1] = next.substring(idx+1);                   
                }
            } else {
                sb.append(line);
            }
            sb.append("\n");
            i++;
        }
        return sb.toString().trim();
    }
    
    public static String cleanAll(String str) {
        if (str == null) {
            return null;
        }
        return clean(cleanHyphenation(str));
    }
    
    public static String cleanAllAndBreaks(String str) {
        if (str == null) {
            return null;
         }
        return clean(cleanHyphenationAndBreaks(str));
    }

    public void setFirstParagraphLineMinScore(double firstParagraphLineMinScore) {
        this.firstParagraphLineMinScore = firstParagraphLineMinScore;
    }

    public void setLastParagraphLineLengthMult(double lastParagraphLineLengthMult) {
        this.lastParagraphLineLengthMult = lastParagraphLineLengthMult;
    }

    public void setMinParagraphIndent(double minParagraphIndent) {
        this.minParagraphIndent = minParagraphIndent;
    }

    public void setParagraphLineIndentMultiplier(double paragraphLineIndentMultiplier) {
        this.paragraphLineIndentMultiplier = paragraphLineIndentMultiplier;
    }
    
}
