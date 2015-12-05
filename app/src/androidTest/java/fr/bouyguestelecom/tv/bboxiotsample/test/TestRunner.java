package fr.bouyguestelecom.tv.bboxiotsample.test;

import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

import junit.framework.TestSuite;

/**
 * Test suite for BboxIoT sample application
 *
 * @author Bertrand Martel
 */
public class TestRunner extends InstrumentationTestRunner {

    @Override
    public TestSuite getAllTests() {
        InstrumentationTestSuite suite = new InstrumentationTestSuite(this);
        suite.addTestSuite(ScanItemArrayAdapterTest.class);
        return suite;
    }

    @Override
    public ClassLoader getLoader() {
        return TestRunner.class.getClassLoader();
    }

}