/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * 
 */
public class MarketDataSwapPVTest {
  private static final double[] FUNDING_TIMES = new double[] {0.0027397260273972603, 0.005479452054794521, 0.0821917808219178, 0.16712328767123288, 0.2493150684931507, 0.33408189235721236,
      0.4187813459091249, 0.4980163185867206, 0.7493824388052998, 1.0034807994610375, 2.0027397260273974, 3.0, 4.0, 5.000748559023879, 10.0 };
  private static final double[] FUNDING_YIELDS = new double[] {0.0013687474974368302, 0.0022305420277649083, 0.0012513719265854874, 0.0010636582500760411, 0.00101828702599128, 9.78165513963152E-4,
      8.959750791691782E-4, 8.943219128571554E-4, 9.938558765821793E-4, 9.239289700537953E-4, 9.936400853191142E-4, 0.0023392448443792023, 0.0044981802665752255, 0.006992645012259607,
      0.017680725566676783 };
  private static final double[] FORWARD_TIMES = new double[] {0.0027397260273972603, 0.019178082191780823, 0.038356164383561646, 0.0821917808219178, 0.16712328767123288, 0.2493150684931507,
      0.49255183771240363, 0.7439179579309829, 0.9925518377124036, 1.238453477056666, 1.484931506849315, 1.7397260273972601, 2.0027397260273974, 3.0, 4.0, 5.000748559023879, 6.005479452054795,
      7.005479452054795, 8.002739726027396, 9.00074855902388, 10.0, 15.0, 20.0, 25.00074855902388, 30.002739726027396 };
  private static final double[] FORWARD_YIELDS = new double[] {0.0014813901115749944, 0.0017545499377800639, 0.00194835932012297, 0.0022782899792324744, 0.002856875387467537, 0.003540919715057364,
      0.004438310251984415, 0.004852425503556177, 0.005017013047413322, 0.005084721597160578, 0.005119824771379274, 0.005137978289853433, 0.005191486919563363, 0.006333160551707626,
      0.008470126926325294, 0.010937769614837807, 0.01326825307368076, 0.015274635199645477, 0.016921786925804233, 0.01832073337206936, 0.01954285376126713, 0.023822140671398637,
      0.025254205542934515, 0.02597355690489602, 0.026370398520735996 };
  private static final double[] FORWARD_BUMPED_YIELDS = new double[] {0.0014813901449354796, 0.00175454999482515, 0.001948359535212128, 0.0022782905824874702, 0.0028568765968019516,
      0.0035409212470074187, 0.00443831145255293, 0.004852426563767288, 0.00501701405289842, 0.005084722579606985, 0.005119825741040314, 0.005137979235589777, 0.005191925200579883,
      0.01629286582680315, 0.008344998893172821, 0.01084192698861046, 0.013188441808502899, 0.015206216995252457, 0.016861893537895873, 0.018267480206991333, 0.019494922517323467,
      0.023790185677964593, 0.025230233662100494, 0.02595437952038944, 0.026354416226774164 };
  private static final DoubleMatrix2D PAR_RATE_JACOBIAN = new DoubleMatrix2D(new double[][] {
      {0.986305068493286, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.9863134246579718, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.25266486647937564, -0.4022566795397808, 1.135648413514797, 3.081667631703691E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.11525947300050361, -0.1834995642279856, -0.0011243715121732629, 1.0558048193142315, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0755204270666958, -0.12023276782628964, -7.367118256785745E-4, 9.210970202019474E-5, 1.0318704505939877, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.055685128666918826, -0.08865385706139976, -5.432158476424117E-4, 6.791726169578331E-5, 0.0, 1.0195402487271055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.044102577336572045, -0.0702137838384585, -4.3022651656964554E-4, 5.3790416905437376E-5, 0.0, 0.0, 1.01219341895065, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.03692070165714102, -0.0587798337846577, -3.6016636266032693E-4, 4.50309268644945E-5, 0.0, 0.0, 0.0, 1.0076871728054515, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.02434646160568817, -0.038760936336379406, -2.3750297601600421E-4, 2.9694552995119413E-5, 0.0, 0.0, 0.0, 0.0, 0.9998885017200277, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0016172541166148, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.174418142066036E-9, 0.0019206490845599111, 0.9990831969742648, 2.4286740240278127E-7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.5671332427689562E-5, 7.418057965225423E-4, 6.05559271714684E-4, 0.999667557183937, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.1770367973073948E-5, 0.0012034115392555815, 0.0017288944022785246, 0.0042939604967844325, 0.9928102437202815, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 9.447588357812495E-6, 0.0015181700136947555, 0.0024764336423496736, 0.005078885001142508, 0.005964100364477698, 0.983339620778448, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.91651104625031E-6, 0.0018426409382298337, 0.003363866451002784, 0.0057542547514947695, -0.03352271075888469, 0.08105716790985673, 0.9262634649107502,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.001906818260323436, -0.054099976916769627, -0.9184312835693518, 1.9557941096830098, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.9299749677693585, 2.9149074817160923, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.7824771869563624E-4, -0.01639100287613763, -2.9036305041005046, 3.9047569207543695, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.3032819113770326E-4, -0.022361007347427977, -3.9201858131637026, 4.927209730316132,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -4.960284829066062, 5.947484796225288, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.032230913984964804, -5.829719339209256, 6.784826763739026,
          3.1782614674671027E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.986305372876844, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.5058036155959064, 1.3620568791404053, 0.13006378686751882, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.15174734794691638, -0.2818165033299876, 1.4199216129318606, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.05837096584654211, -0.10840322228643537, 0.015009676931967975, 1.1382338340075713, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.026633167019268583, -0.049461595892927375, 0.006848528662097637, 0.0, 1.0560050723139995, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.017456409847893633, -0.0324190468603739, 0.004488791103744078, 0.0, 0.0, 1.0325466425029084, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, -1.108629298604767E-4, 0.0, 0.0, 3.330094383002871E-4, -3.548366435462115E-4, 3.247676135001139E-5, 4.1503232140070517E-4, 4.26262411779673E-5, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, -1.5916528726182128E-5, -4.8441708901193585E-5, 0.0020474497189269213, -0.005255348719292596, 0.0036051703772881118, 0.004151344514922252, 4.7614317699085085E-4,
          0.9965164328517292, -4.351773808492648E-5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, -7.400900977209037E-5, 0.0, 0.0, 3.318922842612522E-4, -3.6742639135518764E-4, 6.372724018390768E-4, -2.1309055054214072E-4, -0.0012982938155474382, 1.2699223174868614E-4,
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0625432067477283E-5, -3.233833808969407E-5, 0.0013668205093138762, -0.003508324696193431, 0.002406711513204242, 0.00280890959368736,
          -0.005707373245421088, 0.004293554481951123, 1.0001545805291028, 1.611544609210619E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, -5.568739077806047E-5, 0.0, 0.0, 3.947699566185754E-4, -4.492527134797218E-4, 0.0011381196374841591, 0.0013002079621224029, -0.0025921767252438965, -0.0038235472822847347,
          3.0288592503562614E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -7.995007493670188E-6, -2.4332681599995368E-5, 0.0010284513745019014, -0.0026398062740588465, 0.0018109076845992927,
          0.0021135378879626947, -0.004294459893636679, 0.002852283873741583, 0.006495303714141781, 0.9959407361849362, -4.311421154576528E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, -4.4787664464488155E-5, 0.0, 0.0, 4.4686244707253743E-4, -5.154272842407177E-4, 0.0015027656251858877, 0.0020749755820311942, -7.195150532148575E-5, -0.00649356968200618,
          -0.00470319305303134, 3.003291138798553E-5, 0.0, 0.0, 0.0, 0.0, 0.0, -6.43013989369513E-6, -1.9570031272699278E-5, 8.271519716700334E-4, -0.002123115412697262, 0.001456457640065082,
          0.0016998538526669155, -0.0034539026893711383, 0.00229400464467322, 0.004630368783050603, 0.013033340128934034, 0.9862708173872685, -6.387764836863069E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0 },
      {0.0, 0.0, 0.0, 0.0, -3.755423179413369E-5, 0.0, 0.0, 4.7387537619675985E-4, -5.503398714323212E-4, 0.00171043994007983, 0.0025290074179252177, 0.0011257776693678813, 3.1788158544055877E-4,
          -0.017271444381521207, -2.887040495246602E-4, 0.0, 0.0, 0.0, 0.0, 0.0, -5.3916400179364245E-6, -1.6409372969582083E-5, 6.935627754758628E-4, -0.0017802216143097231, 0.0012212324189556454,
          0.0014253189212360242, -0.002896080064497958, 0.0019235113773611798, 0.003882540977524461, 0.010171918613120879, 0.01757354619324719, 0.973301195148924, -7.89191574105022E-4, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, -3.2480336785418856E-5, 0.0, 0.0, 4.8221900079837145E-4, -5.621961147898146E-4, 0.0018079591188372916, 0.0027631129675072426, 0.0018391077106074043, 0.01024722281111538,
          -0.03093565196649494, -0.001581081090251948, 0.0, 0.0, 0.0, 0.0, 0.0, -4.663183754319506E-6, -1.4192327602669495E-5, 5.998565661727742E-4, -0.0015396985858326913, 0.0010562335685201417,
          0.0012327462545940833, -0.0025047951018697667, 0.001663628687434247, 0.003357976784733062, 0.008797606195732287, 0.014774208890166222, 0.021921314401129904, 0.9575834710693851,
          -0.0014470040376945298, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, -2.8707796662261392E-5, 0.0, 0.0, 4.785578477899668E-4, -5.59259582948534E-4, 0.0018356718232620881, 0.002858685966688819, 0.0022515120037057833, 0.019138743803814747,
          -0.04129205635366752, -0.004194066005743877, 0.0, 0.0, 0.0, 0.0, 0.0, -4.121562282503595E-6, -1.254391103372855E-5, 5.301841677928867E-4, -0.0013608650124311145, 9.335536978343209E-4,
          0.0010895647125474955, -0.0022138670833421747, 0.0014704008273033302, 0.0029679530532450964, 0.007775778048431749, 0.013061304506253327, 0.017041495022803152, 0.02577036340368713,
          0.9404376006434843, -0.0013679309108396293, 0.0, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, -2.5792417068627304E-5, 0.0, 0.0, 4.695255647238893E-4, -5.496006848133035E-4, 0.001828921018139615, 0.002883189901882775, 0.0024960339143395194, 0.024511656160465273,
          -0.0459072353255293, -0.00847268377835605, 0.0, 0.0, 0.0, 0.0, 0.0, -3.7030028676575833E-6, -1.1270031931043603E-5, 4.763420662259963E-4, -0.0012226642952667868, 8.387479754639335E-4,
          9.789155127403809E-4, -0.0019890409500890327, 0.0013210763557384153, 0.0026665467883168047, 0.006986119931728305, 0.011734882242930584, 0.015313985096851269, 0.02097102549723892,
          0.02471295505759205, 0.9250065889606508, -0.0010389886837247758, 0.0, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, -2.3481377973035216E-5, 0.0, 0.0, 4.5822442585479866E-4, -5.370106379853242E-4, 0.001804765022058057, 0.0028696650147991146, 0.0026403444856501752, 0.02431776526204617,
          -0.042622971987527396, -0.014718432699076522, 0.0, 0.0, 0.0, 0.0, 0.0, -3.3712082795240043E-6, -1.0260220235927585E-5, 4.3366110557797963E-4, -0.0011131117481120328, 7.635949040209174E-4,
          8.912032205886023E-4, -0.0018108199099221567, 0.0012027059409668896, 0.002427620213051377, 0.006360153150648572, 0.010683419265520433, 0.013941829157608589, 0.01909199028817661,
          0.02144441749501086, 0.028298546141586445, 0.9045990692964722, -7.07214730548706E-5, 0.0, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, -1.649544253521893E-5, 0.0, 0.0, 3.938070117139512E-4, -4.629092988887371E-4, 0.0015943579024733665, 0.0025880523878538367, 0.0027147543343895306, 0.011592210748755884,
          -0.016876103424326747, -0.03929075994027471, 0.0, 0.0, 0.0, 0.0, 0.0, -2.3682414427719594E-6, -7.207706187205027E-6, 3.046427622363367E-4, -7.819503138761369E-4, 5.36418087299971E-4,
          6.260617042706168E-4, -0.0012720835974810024, 8.448893739868065E-4, 0.0017053798873178684, 0.0044679465120059, 0.007504999441532544, 0.009794001100330057, 0.01341193983773938,
          0.015064497360265697, -0.0022175200676834567, 0.062338813499911265, 0.8985958052890621, -0.002808109130891419, 0.0, 0.0 },
      {0.0, 0.0, 0.0, 0.0, -1.2964171677308962E-5, 0.0, 0.0, 3.2988852683602004E-4, -3.880974219162004E-4, 0.001345614746312882, 0.002196214111806176, 0.0023773821751769354, 0.007553976764628011,
          -0.009559125985485027, -0.043820857441060075, 0.0, 0.0, 0.0, 0.0, 0.0, -1.8612588641894441E-6, -5.664712553847258E-6, 2.3942619674792616E-4, -6.145538739304433E-4, 4.2158409268032177E-4,
          4.920372034532452E-4, -9.997616075120355E-4, 6.640192204188135E-4, 0.0013402997577673607, 0.0035114684254761858, 0.0058983625926010115, 0.007697344972791144, 0.010540771501641184,
          0.011839556871170876, -0.0017428032496821025, 0.046253919387188765, 0.06359700094544873, 0.8620971044377633, -0.002765803698402524, 0.0 },
      {0.0, 0.0, 0.0, 0.0, -1.083963240659773E-5, 0.0, 0.0, 2.8424704510578435E-4, -3.345274772301241E-4, 0.001163332078144736, 0.001903296279181065, 0.0020884735740984237, 0.005673123426552863,
          -0.0064626650799540965, -0.04466435798539861, 0.0, 0.0, 0.0, 0.0, 0.0, -1.5562399514245577E-6, -4.736392212411288E-6, 2.0018957059939838E-4, -5.138421684986107E-4, 3.524958406037155E-4,
          4.114032541808128E-4, -8.359229258455107E-4, 5.552012453563446E-4, 0.0011206544506255486, 0.0029360168846080652, 0.00493175220878397, 0.0064359214062140934, 0.008813373596379778,
          0.00989931694325873, -0.0014571965763646678, 0.038673931200639736, 0.050464625620028444, 0.06419292872640368, 0.8251846804298604, -0.002697066026765522 },
      {0.0, 0.0, 0.0, 0.0, -9.425437811683258E-6, 0.0, 0.0, 2.515602222422754E-4, -2.961220287722899E-4, 0.0010315262813261322, 0.0016899711426530762, 0.00186858925650199, 0.004597193983742645,
          -0.004820464464042041, -0.044670845995201165, 0.0, 0.0, 0.0, 0.0, 0.0, -1.3532048257720104E-6, -4.118457949055742E-6, 1.740718022027871E-4, -4.468036574059712E-4, 3.0650740724980454E-4,
          3.57729454501159E-4, -7.26864090715984E-4, 4.82766814849764E-4, 9.744480658151877E-4, 0.0025529689127723925, 0.0042883302683060474, 0.005596257760385865, 0.00766353522221363,
          0.008607800774686416, -0.0012670831624846408, 0.0336283297986261, 0.043880749146247504, 0.05032726069169849, 0.07236396562942593, 0.7799842540121205 } });
  private static final CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final YieldCurve FUNDING_CURVE = new YieldCurve(InterpolatedDoublesCurve.from(FUNDING_TIMES, FUNDING_YIELDS, INTERPOLATOR));
  private static final YieldCurve FORWARD_CURVE = new YieldCurve(InterpolatedDoublesCurve.from(FORWARD_TIMES, FORWARD_YIELDS, INTERPOLATOR));
  private static final YieldCurve FORWARD_BUMPED_CURVE = new YieldCurve(InterpolatedDoublesCurve.from(FORWARD_TIMES, FORWARD_BUMPED_YIELDS, INTERPOLATOR));
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2011, 5, 16);
  private static final ZonedDateTime MATURITY_DATE = DateUtils.getUTCDate(2018, 5, 16);
  private static final double NOTIONAL = 9.6657e7;
  private static final Currency CURRENCY = Currency.USD;
  private static final SimpleFrequency FIXED_FREQUENCY = SimpleFrequency.SEMI_ANNUAL;
  private static final DayCount FIXED_DAYCOUNT = DayCountFactory.INSTANCE.getDayCount("30U/360");
  private static final BusinessDayConvention FIXED_BUSINESS_DAY_CONVENTION = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean FIXED_EOM = true;
  private static final double FIXED_RATE = 0.03164417;
  private static final boolean FIXED_IS_PAYER = true;
  private static final boolean FLOATING_IS_PAYER = false;
  private static final IborIndex FLOATING_INDEX = new IborIndex(CURRENCY, Period.ofMonths(3), 2, CALENDAR, DayCountFactory.INSTANCE.getDayCount("Actual/360"),
      BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), false);
  private static final AnnuityCouponFixedDefinition FIXED_LEG = AnnuityCouponFixedDefinition.from(CURRENCY, EFFECTIVE_DATE, MATURITY_DATE, FIXED_FREQUENCY, CALENDAR, FIXED_DAYCOUNT,
      FIXED_BUSINESS_DAY_CONVENTION, FIXED_EOM, NOTIONAL, FIXED_RATE, FIXED_IS_PAYER);
  private static final AnnuityCouponIborDefinition FLOATING_LEG = AnnuityCouponIborDefinition.from(EFFECTIVE_DATE, MATURITY_DATE, NOTIONAL, FLOATING_INDEX, FLOATING_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_LEG, FLOATING_LEG);
  private static final double LAST_FIXING = 0.00358056;
  private static final ZonedDateTime LAST_FIXING_DATE = DateUtils.getUTCDate(2011, 5, 16);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward_3m";
  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 23);
  private static final DoubleTimeSeries<ZonedDateTime> TS = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {LAST_FIXING_DATE }, new double[] {LAST_FIXING });
  private static final FixedCouponSwap<Coupon> SWAP = SWAP_DEFINITION.toDerivative(NOW, new DoubleTimeSeries[] {TS }, FUNDING_CURVE_NAME, FORWARD_CURVE_NAME);
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();
  private static final InstrumentSensitivityCalculator SENSITIVITY_CALCULATOR = InstrumentSensitivityCalculator.getInstance();

  @Test
  public void test() {
    final YieldCurveBundle bundle1 = new YieldCurveBundle(new String[] {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME }, new YieldAndDiscountCurve[] {FUNDING_CURVE, FORWARD_CURVE });
    final double pv1 = CALCULATOR.visit(SWAP, bundle1);
    final YieldCurveBundle bundle2 = new YieldCurveBundle(new String[] {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME }, new YieldAndDiscountCurve[] {FUNDING_CURVE, FORWARD_BUMPED_CURVE });
    final double pv2 = CALCULATOR.visit(SWAP, bundle2);
    System.out.println(pv1 + " " + pv2);
    final LinkedHashMap<String, YieldAndDiscountCurve> curves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    curves.put(FUNDING_CURVE_NAME, FUNDING_CURVE);
    curves.put(FORWARD_CURVE_NAME, FORWARD_CURVE);
    final DoubleMatrix1D sensitivities = SENSITIVITY_CALCULATOR.calculateFromParRate(SWAP, null, curves, PAR_RATE_JACOBIAN, PresentValueNodeSensitivityCalculator.getDefaultInstance());
    final DecimalFormat formatter = new DecimalFormat("##.####");
    for (final double element : FUNDING_TIMES) {
      // System.out.println(formatter.format(FUNDING_TIMES[i]) + "\t" + sensitivities.getEntry(i));
    }
    for (int i = FUNDING_TIMES.length; i < FORWARD_TIMES.length + FUNDING_TIMES.length; i++) {
      System.out.println(formatter.format(FORWARD_TIMES[i - FUNDING_TIMES.length]) + "\t" + sensitivities.getEntry(i));
    }
  }

}
