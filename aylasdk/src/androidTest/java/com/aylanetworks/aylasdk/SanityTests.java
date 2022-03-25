package com.aylanetworks.aylasdk;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
        LanModeTest.class,
        PropertyTest.class,
        SignInTest.class,
})
public class SanityTests
{
}