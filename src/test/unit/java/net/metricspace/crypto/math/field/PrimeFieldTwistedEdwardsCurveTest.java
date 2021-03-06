/* Copyright (c) 2018, Eric McCorkle.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.metricspace.crypto.math.field;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = "unit")
public abstract class
    PrimeFieldTwistedEdwardsCurveTest<P extends PrimeField<P>> {
    private final int avalue;
    private final int dvalue;

    protected PrimeFieldTwistedEdwardsCurveTest(final int avalue,
                                                final int dvalue) {
        this.avalue = avalue;
        this.dvalue = dvalue;
    }

    @DataProvider(name = "sanityPoints")
    protected abstract Object[][] getSanityPoints();

    @DataProvider(name = "addPoints")
    protected abstract Object[][] getAddPoints();

    @DataProvider(name = "doublePoints")
    protected abstract Object[][] getDoublePoints();

    private P addScalars(final P a,
                         final P b) {
        final P out = a.clone();

        out.add(b);

        return out;
    }

    private P subScalars(final P a,
                         final P b) {
        final P out = a.clone();

        out.sub(b);

        return out;
    }

    private P mulScalars(final P a,
                         final P b) {
        final P out = a.clone();

        out.mul(b);

        return out;
    }

    private P divScalars(final P a,
                         final P b) {
        final P out = a.clone();

        out.div(b);

        return out;
    }

    private P scalarTimesA(final P s) {
        final P out = s.clone();

        out.mul(avalue);

        return out;
    }

    private P scalarTimesD(final P s) {
        final P out = s.clone();

        out.mul(dvalue);

        return out;
    }

    private P onePlusScalar(final P s) {
        final P out = s.clone();

        out.add(1);

        return out;
    }

    private P oneMinusScalar(final P s) {
        final P out = s.clone();

        out.neg();
        out.add(1);

        return out;
    }

    private P scalarPlusOne(final P s) {
        return onePlusScalar(s);
    }

    private P scalarMinusOne(final P s) {
        final P out = s.clone();

        out.sub(1);

        return out;
    }

    private P edwardsToMontgomeryX(final P x,
                                   final P y) {
        final P numer = onePlusScalar(y);
        final P denom = oneMinusScalar(y);

        return divScalars(numer, denom);
    }

    private P edwardsToMontgomeryY(final P x,
                                   final P y) {
        final P numer = onePlusScalar(y);
        final P onemy = oneMinusScalar(y);
        final P denom = mulScalars(x, onemy);

        return divScalars(numer, denom);
    }

    private P montgomeryToEdwardsX(final P u,
                                   final P v) {
        return divScalars(u, v);
    }

    private P montgomeryToEdwardsY(final P u,
                                   final P v) {
        final P numer = scalarMinusOne(u);
        final P denom = scalarPlusOne(u);

        return divScalars(numer, denom);
    }

    private P additionX(final P x1,
                        final P x2,
                        final P y1,
                        final P y2) {
        final P x1y2 = mulScalars(x1, y2);
        final P x2y1 = mulScalars(x2, y1);
        final P x1x2y1y2 = mulScalars(x1y2, x2y1);
        final P dx1x2y1y2 = scalarTimesD(x1x2y1y2);
        final P x1y2plusx2y1 = addScalars(x1y2, x2y1);
        final P oneplusdx1x2y1y2 = onePlusScalar(dx1x2y1y2);

        return divScalars(x1y2plusx2y1, oneplusdx1x2y1y2);
    }

    private P additionY(final P x1,
                        final P x2,
                        final P y1,
                        final P y2) {
        final P x1x2 = mulScalars(x1, x2);
        final P y1y2 = mulScalars(y1, y2);
        final P ax1x2 = scalarTimesA(x1x2);
        final P x1x2y1y2 = mulScalars(x1x2, y1y2);
        final P dx1x2y1y2 = scalarTimesD(x1x2y1y2);
        final P y1y2minusx1x2 = subScalars(y1y2, ax1x2);
        final P oneminusdx1x2y1y2 = oneMinusScalar(dx1x2y1y2);

        return divScalars(y1y2minusx1x2, oneminusdx1x2y1y2);
    }

    private P doubleX(final P x,
                      final P y) {
        return additionX(x, x, y, y);
    }

    private P doubleY(final P x,
                      final P y) {
        return additionY(x, x, y, y);
    }

    @Test(dataProvider = "sanityPoints",
          description = "Sanity check of birational equivalence")
    public void testBirationalSanity(final P x,
                                     final P y) {
        final P edwardsX = montgomeryToEdwardsX(x, y);
        final P edwardsY = montgomeryToEdwardsY(x, y);
        final P montgomeryX = edwardsToMontgomeryX(edwardsX, edwardsY);
        final P montgomeryY = edwardsToMontgomeryY(edwardsX, edwardsY);

        Assert.assertEquals(montgomeryX, x);
        Assert.assertEquals(montgomeryY, y);
    }

    @Test(dataProvider = "doublePoints",
          description = "Test double according to Edwards curve formula")
    public void testDouble(final P montgomeryX,
                           final P montgomeryY,
                           final P expectedX,
                           final P expectedY) {
        final P x = montgomeryToEdwardsX(montgomeryX, montgomeryY);
        final P y = montgomeryToEdwardsY(montgomeryX, montgomeryY);
        final P actualX = doubleX(x, y);
        final P actualY = doubleY(x, y);

        Assert.assertEquals(edwardsToMontgomeryX(actualX, actualY), expectedX);
        Assert.assertEquals(edwardsToMontgomeryY(actualX, actualY), expectedY);
    }

    @Test(dataProvider = "addPoints",
          description = "Test double according to Edwards curve formula")
    public void testAddition(final P montgomeryX1,
                             final P montgomeryY1,
                             final P montgomeryX2,
                             final P montgomeryY2,
                             final P expectedX,
                             final P expectedY) {
        final P x1 = montgomeryToEdwardsX(montgomeryX1, montgomeryY1);
        final P y1 = montgomeryToEdwardsY(montgomeryX1, montgomeryY1);
        final P x2 = montgomeryToEdwardsX(montgomeryX2, montgomeryY2);
        final P y2 = montgomeryToEdwardsY(montgomeryX2, montgomeryY2);
        final P actualX = additionX(x1, x2, y1, y2);
        final P actualY = additionY(x1, x2, y1, y2);

        Assert.assertEquals(actualX, montgomeryToEdwardsX(expectedX,
                                                          expectedY));
        Assert.assertEquals(actualY, montgomeryToEdwardsY(expectedX,
                                                          expectedY));
    }
}
