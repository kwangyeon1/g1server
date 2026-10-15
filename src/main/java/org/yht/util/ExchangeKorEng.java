package org.yht.util;

import java.util.HashMap;
 
public class ExchangeKorEng
{
    public static String convert( String hangul )
    {
        if( hangul == null || hangul.length() <= 0 )
            return "";
 
        StringBuffer alpha = new StringBuffer();
 
        for( int i = 0; i < hangul.length(); i++ )
        {
            char c = hangul.charAt( i );
 
            // 자음 혹은 모음 한글자로 이루어진 한글이면 바로 KEY_MAP에서 찾아서 추가한다.
            if( isSingleHangul( c ) )
            {
                String s1 = (String)CHOSUNG_KEY_MAP.get( new Character(c));
                String s2 = (String)JUNGSUNG_KEY_MAP.get( new Character(c));
                String s3 = (String)JONGSUNG_KEY_MAP.get( new Character(c));
 
                if(s1 != null)
                    alpha.append( s1 );
                else if(s2 != null)
                    alpha.append( s2 );
                else if(s3 != null)
                    alpha.append( s3 );
                continue;
            }
 
            // 한글이 아니면 그 글자를 바로 추가한다.
            if( !isHangul( c ) )
            {
            	if(c == '·'){
            		alpha.append(' ');
            	}else if(c=='㈜'){
            	}else if(c=='∥'){
            	}else{
            		alpha.append( c );
            	}
                
                continue;
            }
 
            int code = (int)c;
 
            // 초성분리
            code -= 0xAC00;
            int chosung = code / ( 21 * 28 );
            alpha.append( (String)CHOSUNG_KEY_MAP.get(  new Character(CHOSUNG[chosung]) ) );
 
            // 중성분리
            code -= chosung * 21 * 28;
            int jungsung = code / 28;
            alpha.append( (String)JUNGSUNG_KEY_MAP.get(  new Character(JUNGSUNG[jungsung]) ) );
 
            // 종성분리
            int jongsung = ( code % 28 ) - 1;
            if( jongsung >= 0 )
                alpha.append( (String)JONGSUNG_KEY_MAP.get(  new Character(JONGSUNG[jongsung]) ) );
        }
 
        return alpha.toString();
    }
 
    /**
     *
     * @param c
     * @return
     */
    private static boolean isSingleHangul( char c )
    {
        int code = (int)c;
        if( code >= 0x3131 && code <= 0x3163 )
            return true;
        return false;
    }
 
    /**
     *
     * @param c
     * @return
     */
    private static boolean isHangul( char c )
    {
        int code = (int)c;
        if( code >= 0xAC00 && code <= 0xD7A3 )
            return true;
        return false;
    }
 
    // --------------------------------------------------------------------------------
 
    private static final char[] CHOSUNG =  { 'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
                                             'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ' };
    private static final char[] JUNGSUNG = { 'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
                                             'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ',
                                             'ㅣ' };
    private static final char[] JONGSUNG = { 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ',
                                             'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ',
                                             'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ' };
    
    private static final HashMap<Character, String> CHOSUNG_KEY_MAP;
    static
    {
        CHOSUNG_KEY_MAP = new HashMap<Character, String>();
        CHOSUNG_KEY_MAP.put( new Character( 'ㄱ' ), "G" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㄲ' ), "GG" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㄴ' ), "N" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㄷ' ), "D" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㄸ' ), "DD" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㄹ' ), "R" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅁ' ), "M" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅂ' ), "B" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅃ' ), "BB" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅅ' ), "S" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅆ' ), "SS" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅇ' ), "" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅈ' ), "J" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅉ' ), "JJ" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅊ' ), "CH" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅋ' ), "K" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅌ' ), "T" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅍ' ), "P" );
        CHOSUNG_KEY_MAP.put( new Character( 'ㅎ' ), "H" );
    }
 
    private static final HashMap<Character, String> JUNGSUNG_KEY_MAP;
    static
    {
        JUNGSUNG_KEY_MAP = new HashMap<Character, String>();
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅏ' ), "A" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅐ' ), "AE" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅑ' ), "YA" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅒ' ), "YAE" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅓ' ), "EO" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅔ' ), "E" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅕ' ), "YEO" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅖ' ), "YE" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅗ' ), "O" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅘ' ), "WA" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅙ' ), "WAE" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅚ' ), "OE" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅛ' ), "YO" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅜ' ), "U" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅝ' ), "WO" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅞ' ), "WE" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅟ' ), "WI" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅠ' ), "YU" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅡ' ), "EU" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅢ' ), "UI" );
        JUNGSUNG_KEY_MAP.put( new Character( 'ㅣ' ), "I" );
 
    }
 
    private static final HashMap<Character, String> JONGSUNG_KEY_MAP;
    static
    {
        JONGSUNG_KEY_MAP = new HashMap<Character, String>();
        JONGSUNG_KEY_MAP.put( new Character( 'ㄱ' ), "K" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄲ' ), "KK" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄳ' ), "KS" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄴ' ), "N" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄵ' ), "NC" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄶ' ), "NH" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄷ' ), "D" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄹ' ), "L" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄺ' ), "LK" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄻ' ), "LM" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄼ' ), "LP" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄽ' ), "LS" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄾ' ), "LTH" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㄿ' ), "LPH" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅀ' ), "LH" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅁ' ), "M" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅂ' ), "P" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅄ' ), "PS" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅅ' ), "S" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅆ' ), "SS" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅇ' ), "NG" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅈ' ), "C" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅊ' ), "CH" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅋ' ), "KH" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅌ' ), "TH" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅍ' ), "PH" );
        JONGSUNG_KEY_MAP.put( new Character( 'ㅎ' ), "H" );
    }  
 
}