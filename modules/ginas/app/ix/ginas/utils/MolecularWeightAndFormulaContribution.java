package ix.ginas.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import gov.nih.ncats.common.util.SingleThreadCounter;
import ix.core.validator.GinasProcessingMessage;
import play.Logger;

public class MolecularWeightAndFormulaContribution
{

  private Double mw = 0.0;
  private Double mwHigh = 0.0;
  private Double mwHighLimit = 0.0;
  private Double mwLow = 0.0;
  private Double mwLowLimit = 0.0;

  private String substanceClass;
  private String formula;
  private Map<String, SingleThreadCounter> formulaMap = new HashMap<>();
  private List<GinasProcessingMessage> messages = new ArrayList<>();

  public static Map<String, SingleThreadCounter> parseMapFromFormula(String formula) {
    Map<String, SingleThreadCounter> map = new HashMap<>();
    String[] formulaParts = formula.split("\\.");
    if (formulaParts.length > 1) {
      //dealing with the individual components of a multipart formula
      List<Map<String, SingleThreadCounter>> maps = new ArrayList<>();
      for (String part : formulaParts) {
        //System.out.println("examining dotted formula part " + part);
        long multiplier = 1;
        if (Character.isDigit(part.charAt(0))) {
          int pos = 0;
          StringBuilder multiplierBuilder = new StringBuilder();
          while (Character.isDigit(part.charAt(pos))) {
            multiplierBuilder.append(part.charAt(pos));
            pos++;
          }
          multiplier = Long.parseLong(multiplierBuilder.toString());
          part = part.substring(pos);
          //System.out.println(String.format("multiplier: %d; revised part: %s", multiplier, part));
        }
        final Map<String, SingleThreadCounter> partMap = parseMapFromFormula(part);
        for (Map.Entry<String, SingleThreadCounter> entry : partMap.entrySet()) {
          entry.setValue(new SingleThreadCounter(multiplier * entry.getValue().getAsLong()));
        }

        maps.add(partMap);
      }

      //now we merge the individual maps into one
      // note: there are more concise ways of doing this. This way is understandable 
      for (Map<String, SingleThreadCounter> partMap : maps) {
        for (Map.Entry<String, SingleThreadCounter> entry : partMap.entrySet()) {
          String symbol = entry.getKey();
          map.computeIfAbsent(symbol, new Function<String, SingleThreadCounter>()
          {

            @Override
            public SingleThreadCounter apply(String ignored) {
              return new SingleThreadCounter();
            }
          })
                  .increment(entry.getValue().getAsLong());
        }
      }
      return map;
    }
    String trimmed = formula.trim();
    int pos = 0;
    int previousPos = 0;
    StringBuilder symbolBuilder = new StringBuilder();
    while (pos < trimmed.length()) {
      if (pos < trimmed.length() && Character.isAlphabetic(trimmed.charAt(pos))) {
        symbolBuilder.append(trimmed.charAt(pos));
        pos++;
        if (pos < trimmed.length() && Character.isLowerCase(trimmed.charAt(pos))) {
          symbolBuilder.append(trimmed.charAt(pos));
          pos++;
        }
      }

      StringBuilder numberBuilder = new StringBuilder();
      while (pos < trimmed.length() && Character.isDigit(trimmed.charAt(pos))) {
        numberBuilder.append(trimmed.charAt(pos));
        pos++;
      }
      int count = 1;
      if (numberBuilder.length() > 0) {
        count = Integer.parseInt(numberBuilder.toString());
      }
      String symbol = symbolBuilder.toString();
      if (map.containsKey(symbol)) {
        map.get(symbol).increment(count);
      }
      else {
        map.put(symbol, new SingleThreadCounter(count));
      }

      symbolBuilder = new StringBuilder();
      if (pos == previousPos) {
        pos++;
      }
      previousPos = pos;
    }
    return map;
  }

  public Double getMw() {
    return mw;
  }

  public void setMw(Double mw) {
    this.mw = mw;
  }

  public String getSubstanceClass() {
    return substanceClass;
  }

  public void setSubstanceClass(String substanceClass) {
    this.substanceClass = substanceClass;
  }

  public MolecularWeightAndFormulaContribution(Double mw, String substanceClass, Map<String, SingleThreadCounter> formulaMap) {
    this.mw = mw;
    this.substanceClass = substanceClass;
    this.formulaMap = formulaMap;
  }

  public MolecularWeightAndFormulaContribution(Double mw, String substanceClass, String rawFormula) {
    this.mw = mw;
    this.substanceClass = substanceClass;
    this.formulaMap = parseMapFromFormula(rawFormula);
  }

  public MolecularWeightAndFormulaContribution(Double avg, Double mwHigh, Double mwLow, String substanceClass, String rawFormula) {
    this.mw = avg;
    this.mwHigh = mwHigh;
    this.mwLow = mwLow;
    this.substanceClass = substanceClass;
    if (rawFormula != null && rawFormula.length() > 0) {
      this.formulaMap = parseMapFromFormula(rawFormula);
    }
  }

  public MolecularWeightAndFormulaContribution(Double avg, Double mwHigh, Double mwLow, Double mwHighLimit, Double mwLowLimit,
          String substanceClass, String rawFormula) {
    Logger.trace("7-parameter constructor");
    this.mw = avg;
    this.mwHigh = mwHigh;
    this.mwLow = mwLow;
    this.mwHighLimit = mwHighLimit;
    this.mwLowLimit = mwLowLimit;

    this.substanceClass = substanceClass;
    if (rawFormula != null && rawFormula.length() > 0) {
      this.formulaMap = parseMapFromFormula(rawFormula);
    }
    Logger.trace("end of 7-parameter constructor");
  }

  public MolecularWeightAndFormulaContribution(String substanceClass, GinasProcessingMessage message) {
    this.mw = 0.0;
    this.substanceClass = substanceClass;
    this.messages.add(message);
  }

  public String getFormula() {
    return formula;
  }

  public void setFormula(String formula) {
    this.formula = formula;
  }

  public Map<String, SingleThreadCounter> getFormulaMap() {
    return formulaMap;
  }

  public void setFormulaMap(Map<String, SingleThreadCounter> formulaMap) {
    this.formulaMap = formulaMap;
  }

  public void setFormulaMap(String rawFormula) {
    this.formulaMap = parseMapFromFormula(rawFormula);
  }

  public List<GinasProcessingMessage> getMessages() {
    return messages;
  }

  public void setMessages(List<GinasProcessingMessage> messages) {
    this.messages = messages;
  }

  public Double getMwHigh() {
    return mwHigh;
  }

  public void setMwHigh(Double mwHigh) {
    this.mwHigh = mwHigh;
  }

  public Double getMwLow() {
    return mwLow;
  }

  public void setMwLow(Double mwLow) {
    this.mwLow = mwLow;
  }

  public Double getMwHighLimit() {
    return mwHighLimit;
  }

  public void setMwHighLimit(Double mwHighLimit) {
    this.mwHighLimit = mwHighLimit;
  }

  public Double getMwLowLimit() {
    return mwLowLimit;
  }

  public void setMwLowLimit(Double mwLowLimit) {
    this.mwLowLimit = mwLowLimit;
  }

}
