package com.nyble.main;


import com.nyble.utils.StringConverter;

public class ComparatorUtils {

    private String s;

    public ComparatorUtils (String s){
        this.s = s;
    }

    public static String getFullName(String firstName, String lastName) {
        String fullName = (firstName!=null ? firstName : "")+(lastName!=null ? lastName : "");
        return fullName.isEmpty()?null : fullName;
    }

    public static String getLocation(String county, String city, String street) {
        String location = (county!=null ? county : "")+(city!=null ? city : "")+(street!=null ? street : "");
        return location.isEmpty() ? null : location;
    }

    public ComparatorUtils toUpper(){
        if(s == null){return this;}
        s = s.toUpperCase();
        return this;
    }

    public ComparatorUtils removeWhiteSpaces(){
        if(s == null){return this;}
        s = s.replace(" ", "");
        return this;
    }

    public ComparatorUtils removeExtraCharsAndDuplicates(){
        if(s == null){return this;}
        StringBuilder sb = new StringBuilder();
        for(char c: s.toCharArray()){
            if(c == '_' || c =='-' || c=='\'' || c == '"' || c == '.'){
                c = ' ';
            }
            if(sb.length()>0 && c == sb.charAt(sb.length()-1)) continue;
            sb.append(c);
        }
        s = sb.toString();
        return this;
    }

    public ComparatorUtils replaceDiacritics (){
        if(s == null){return this;}
        s = s.replaceAll("\u00c2", "A");
        s = s.replaceAll("\u00c3", "A");
        s = s.replaceAll("\u0102", "A");
        s = s.replaceAll("\u0100", "A");
        s = s.replaceAll("\u00ce", "I");
        s = s.replaceAll("\u020a", "I");
        s = s.replaceAll("\u015e", "S");
        s = s.replaceAll("\u0218", "S");
        s = s.replaceAll("\u0162", "T");
        s = s.replaceAll("\u021a", "T");
        return this;
    }

    public ComparatorUtils convertToNull(){
        s = new StringConverter(s).consumerAttributeNullEquivalence().get();
        return this;
    }

    public ComparatorUtils formatAsPhone(){
        if(s == null){return this;}
        if(s.length() <= 10){
            s = "004"+s;
        }else if(s.startsWith("+4")){
            s = s.replace("+", "00");
        }
        return this;
    }

    public String getText(){ return s;}
}
