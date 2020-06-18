package br.eti.meta.util.cjkv;

import android.app.Application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import br.eti.meta.util.cjkv.CJKVUtil;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, application = Application.class)
public class CJKVUtilTest {

  private static final String CJKV_CHARS    = "统码";
  private static final String NON_CJKV_CHAR = "a";
  private static final String MIXED_CHARS   = CJKV_CHARS + NON_CJKV_CHAR;

  @Test
  public void givenAllCJKVChars_whenIsCJKV_thenIExpectTrue() {
    // WHEN
    boolean result = CJKVUtil.isCJKV(CJKV_CHARS);

    //THEN
    assertTrue(result);
  }

  @Test
  public void givenNoCJKVChars_whenIsCJKV_thenIExpectFalse() {
    // WHEN
    boolean result = CJKVUtil.isCJKV(NON_CJKV_CHAR);

    // THEN
    assertFalse(result);
  }

  @Test
  public void givenOneNonCJKVChar_whenIsCJKV_thenIExpectFalse() {
    // WHEN
    boolean result = CJKVUtil.isCJKV(MIXED_CHARS);

    // THEN
    assertFalse(result);
  }

  @Test
  public void givenAnEmptyString_whenIsCJKV_thenIExpectTrue() {
    // WHEN
    boolean result = CJKVUtil.isCJKV("");

    // THEN
    assertTrue(result);
  }

  @Test
  public void givenNull_whenIsCJKV_thenIExpectTrue() {
    // WHEN
    boolean result = CJKVUtil.isCJKV(null);

    // THEN
    assertTrue(result);
  }

}