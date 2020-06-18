package br.eti.meta.util.livedata;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import br.eti.meta.util.DefaultValueLiveData;

import static org.junit.Assert.assertEquals;

public final class LiveDataUtilTest {

  @Rule
  public TestRule rule = new LiveDataRule();

  @Test
  public void initially_no_value() {
    MutableLiveData<String> liveDataA = new MutableLiveData<>();
    MutableLiveData<String> liveDataB = new MutableLiveData<>();

    LiveData<String> combined = LiveDataUtil.combineLatest(liveDataA, liveDataB, (a, b) -> a + b);

    LiveDataTestUtil.assertNoValue(combined);
  }

  @Test
  public void no_value_after_just_a() {
    MutableLiveData<String> liveDataA = new MutableLiveData<>();
    MutableLiveData<String> liveDataB = new MutableLiveData<>();

    LiveData<String> combined = LiveDataUtil.combineLatest(liveDataA, liveDataB, (a, b) -> a + b);

    liveDataA.setValue("Hello, ");

    LiveDataTestUtil.assertNoValue(combined);
  }

  @Test
  public void no_value_after_just_b() {
    MutableLiveData<String> liveDataA = new MutableLiveData<>();
    MutableLiveData<String> liveDataB = new MutableLiveData<>();

    LiveData<String> combined = LiveDataUtil.combineLatest(liveDataA, liveDataB, (a, b) -> a + b);

    liveDataB.setValue("World!");

    LiveDataTestUtil.assertNoValue(combined);
  }

  @Test
  public void combined_value_after_a_and_b() {
    MutableLiveData<String> liveDataA = new MutableLiveData<>();
    MutableLiveData<String> liveDataB = new MutableLiveData<>();

    LiveData<String> combined = LiveDataUtil.combineLatest(liveDataA, liveDataB, (a, b) -> a + b);

    liveDataA.setValue("Hello, ");
    liveDataB.setValue("World!");

    Assert.assertEquals("Hello, World!", LiveDataTestUtil.getValue(combined));
  }

  @Test
  public void on_update_a() {
    MutableLiveData<String> liveDataA = new MutableLiveData<>();
    MutableLiveData<String> liveDataB = new MutableLiveData<>();

    LiveData<String> combined = LiveDataUtil.combineLatest(liveDataA, liveDataB, (a, b) -> a + b);

    liveDataA.setValue("Hello, ");
    liveDataB.setValue("World!");

    Assert.assertEquals("Hello, World!", LiveDataTestUtil.getValue(combined));

    liveDataA.setValue("Welcome, ");
    Assert.assertEquals("Welcome, World!", LiveDataTestUtil.getValue(combined));
  }

  @Test
  public void on_update_b() {
    MutableLiveData<String> liveDataA = new MutableLiveData<>();
    MutableLiveData<String> liveDataB = new MutableLiveData<>();

    LiveData<String> combined = LiveDataUtil.combineLatest(liveDataA, liveDataB, (a, b) -> a + b);

    liveDataA.setValue("Hello, ");
    liveDataB.setValue("World!");

    Assert.assertEquals("Hello, World!", LiveDataTestUtil.getValue(combined));

    liveDataB.setValue("Joe!");
    Assert.assertEquals("Hello, Joe!", LiveDataTestUtil.getValue(combined));
  }

  @Test
  public void combined_same_instance() {
    MutableLiveData<String> liveDataA = new MutableLiveData<>();

    LiveData<String> combined = LiveDataUtil.combineLatest(liveDataA, liveDataA, (a, b) -> a + b);

    liveDataA.setValue("Echo! ");

    Assert.assertEquals("Echo! Echo! ", LiveDataTestUtil.getValue(combined));
  }

  @Test
  public void on_a_set_before_combine() {
    MutableLiveData<String> liveDataA = new MutableLiveData<>();
    MutableLiveData<String> liveDataB = new MutableLiveData<>();

    liveDataA.setValue("Hello, ");

    LiveData<String> combined = LiveDataUtil.combineLatest(liveDataA, liveDataB, (a, b) -> a + b);

    liveDataB.setValue("World!");

    Assert.assertEquals("Hello, World!", LiveDataTestUtil.getValue(combined));
  }

  @Test
  public void on_default_values() {
    MutableLiveData<Integer> liveDataA = new DefaultValueLiveData<>(10);
    MutableLiveData<Integer> liveDataB = new DefaultValueLiveData<>(30);

    LiveData<Integer> combined = LiveDataUtil.combineLatest(liveDataA, liveDataB, (a, b) -> a * b);

    Assert.assertEquals(Integer.valueOf(300), LiveDataTestUtil.getValue(combined));
  }
}
