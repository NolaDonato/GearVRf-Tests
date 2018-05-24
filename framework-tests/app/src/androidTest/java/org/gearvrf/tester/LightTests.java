package org.gearvrf.tester;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.jodah.concurrentunit.Waiter;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSpotLight;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.unittestutils.GVRTestUtils;
import org.gearvrf.unittestutils.GVRTestableActivity;

import org.joml.Vector3f;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class LightTests
{
    private static final String TAG = LightTests.class.getSimpleName();
    private GVRTestUtils mTestUtils;
    private Waiter mWaiter;
    private GVRSceneObject mRoot;
    private GVRSceneObject mSphere;
    private GVRSceneObject mCube;
    private boolean mDoCompare = true;

    @Rule

    public ActivityTestRule<GVRTestableActivity> ActivityRule = new ActivityTestRule<GVRTestableActivity>(GVRTestableActivity.class);

    @After
    public void tearDown()
    {
        GVRScene scene = mTestUtils.getMainScene();
        if (scene != null)
        {
            scene.clear();
        }
    }

    @Before
    public void setUp() throws TimeoutException
    {
        GVRTestableActivity activity = ActivityRule.getActivity();
        mTestUtils = new GVRTestUtils(activity);
        mTestUtils.waitForOnInit();
        mWaiter = new Waiter();

        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRScene scene = mTestUtils.getMainScene();
        GVRMaterial white = new GVRMaterial(ctx, GVRMaterial.GVRShaderType.Phong.ID);
        GVRMaterial blue = new GVRMaterial(ctx, GVRMaterial.GVRShaderType.Phong.ID);
        GVRMaterial check = new GVRMaterial(ctx, GVRMaterial.GVRShaderType.Phong.ID);
        GVRTexture checker = ctx.getAssetLoader().loadTexture(new GVRAndroidResource(ctx, R.drawable.checker));
        TextureEventHandler texHandler = new TextureEventHandler(mTestUtils, 1);
        GVRSceneObject background = new GVRCubeSceneObject(ctx, false, white);

        ctx.getEventReceiver().addListener(texHandler);
        mWaiter.assertNotNull(scene);
        mWaiter.assertNotNull(checker);
        check.setTexture("diffuseTexture", checker);
        background.getTransform().setScale(10, 10, 10);
        blue.setDiffuseColor(0, 0, 1, 1);
        mSphere = new GVRSphereSceneObject(ctx, true, blue);
        mSphere.getTransform().setPosition(0, 0, -2);
        mCube = new GVRCubeSceneObject(ctx, true, check, new Vector3f(6, 3, 1));
        mCube.getTransform().setPosition(-1, 0, -4);
        mRoot = scene.getRoot();
        mWaiter.assertNotNull(mRoot);
        mTestUtils.waitForAssetLoad();
        ctx.getEventReceiver().removeListener(texHandler);
        mRoot.addChildObject(background);
    }

    public void point() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRPointLight light = new GVRPointLight(ctx);

        lightObj.attachComponent(light);
        mRoot.addChildObject(lightObj);
        mRoot.addChildObject(mCube);
        mRoot.addChildObject(mSphere);
    }

    @Test
    public void pointLightAtFrontIlluminates() throws TimeoutException
    {
        GVRLight.setDefaultQuality(2);
        point();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "pointLightAtFrontIlluminates", mWaiter, mDoCompare);
    }

    @Test
    public void vertexPoint() throws TimeoutException
    {
        GVRLight.setDefaultQuality(1);
        point();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "vertexPoint", mWaiter, mDoCompare);
    }

    @Test
    public void canDisableLightInRenderData() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRPointLight light = new GVRPointLight(ctx);

        lightObj.attachComponent(light);
        mRoot.addChildObject(lightObj);
        mRoot.addChildObject(mCube);
        mRoot.addChildObject(mSphere);
        mTestUtils.waitForXFrames(2);
        mSphere.getRenderData().disableLight();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "canDisableLightInRenderData", mWaiter, mDoCompare);
    }

    @Test
    public void pointLightIlluminatesInColor() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRPointLight light = new GVRPointLight(ctx);

        light.setDiffuseIntensity(0, 0, 1, 1);
        lightObj.attachComponent(light);
        mRoot.addChildObject(lightObj);
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(),"pointLightIlluminatesInColor", mWaiter, mDoCompare);
    }

    @Test
    public void pointLightAtFrontAttenuates() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRPointLight light = new GVRPointLight(ctx);

        light.setAttenuation(0, 1, 0);
        lightObj.attachComponent(light);
        mRoot.addChildObject(lightObj);
        mRoot.addChildObject(mCube);
        mRoot.addChildObject(mSphere);
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "pointLightAtFrontAttenuates", mWaiter, mDoCompare);
    }

    @Test
    public void pointLightAtCornerIlluminates() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRPointLight light = new GVRPointLight(ctx);

        lightObj.getTransform().setPosition(-3, 0, 3);
        lightObj.attachComponent(light);
        mRoot.addChildObject(lightObj);
        mRoot.addChildObject(mCube);
        mRoot.addChildObject(mSphere);
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "pointLightAtCornerIlluminates", mWaiter, mDoCompare);
    }

    public void pointSpecular() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRPointLight light = new GVRPointLight(ctx);

        lightObj.getTransform().setPosition(-3, 0, 3);
        lightObj.attachComponent(light);
        mSphere.getRenderData().getMaterial().setSpecularColor(0.8f, 0.8f, 0.8f, 1.0f);
        mSphere.getRenderData().getMaterial().setSpecularExponent(8.0f);
        mRoot.addChildObject(lightObj);
        mRoot.addChildObject(mCube);
        mRoot.addChildObject(mSphere);
    }

    @Test
    public void pointLightHasSpecularReflection() throws TimeoutException
    {
        GVRLight.setDefaultQuality(2);
        pointSpecular();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "pointLightHasSpecularReflection", mWaiter, mDoCompare);
    }

    @Test
    public void vertexPointSpecular() throws TimeoutException
    {
        GVRLight.setDefaultQuality(1);
        pointSpecular();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "vertexPointSpecular", mWaiter, mDoCompare);
    }

    @Test
    public void spotLightAtFrontIlluminates() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRSpotLight light = new GVRSpotLight(ctx);

        lightObj.attachComponent(light);
        light.setInnerConeAngle(30.0f);
        light.setOuterConeAngle(45.0f);
        mRoot.addChildObject(lightObj);
        mRoot.addChildObject(mCube);
        mRoot.addChildObject(mSphere);
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "spotLightAtFrontIlluminates", mWaiter, mDoCompare);
   }

    @Test
    public void spotLightIlluminatesInColor() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRSpotLight light = new GVRSpotLight(ctx);

        light.setDiffuseIntensity(1, 0, 0, 1);
        lightObj.attachComponent(light);
        light.setInnerConeAngle(30.0f);
        light.setOuterConeAngle(45.0f);
        mRoot.addChildObject(lightObj);
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "spotLightIlluminatesInColor", mWaiter, mDoCompare);
    }

    @Test
    public void spotLightAtCornerIlluminates() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRSpotLight light = new GVRSpotLight(ctx);

        lightObj.attachComponent(light);
        lightObj.getTransform().rotateByAxis(-45, 0, 1, 0);
        lightObj.getTransform().setPosition(-3, 0, 3);
        light.setInnerConeAngle(30.0f);
        light.setOuterConeAngle(45.0f);
        mRoot.addChildObject(lightObj);
        mRoot.addChildObject(mCube);
        mRoot.addChildObject(mSphere);
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "spotLightAtCornerIlluminates", mWaiter, mDoCompare);
    }

    public void spotSpecular() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRSpotLight light = new GVRSpotLight(ctx);

        lightObj.attachComponent(light);
        lightObj.getTransform().rotateByAxis(-45, 0, 1, 0);
        lightObj.getTransform().setPosition(-3, 0, 3);
        light.setInnerConeAngle(30.0f);
        light.setOuterConeAngle(45.0f);
        mRoot.addChildObject(lightObj);
        mSphere.getRenderData().getMaterial().setSpecularColor(0.8f, 0.8f, 0.8f, 1.0f);
        mSphere.getRenderData().getMaterial().setSpecularExponent(8.0f);
        mRoot.addChildObject(mSphere);
    }

    @Test
    public void spotLightHasSpecularReflection() throws TimeoutException
    {
        GVRLight.setDefaultQuality(2);
        spotSpecular();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "spotLightHasSpecularReflection", mWaiter, mDoCompare);
    }

    @Test
    public void vertexSpotSpecular() throws TimeoutException
    {
        GVRLight.setDefaultQuality(1);
        spotSpecular();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "vertexSpotSpecular", mWaiter, mDoCompare);
    }

    public void spotAttenuation() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRSpotLight light = new GVRSpotLight(ctx);

        light.setAttenuation(0, 1, 0);
        lightObj.attachComponent(light);
        light.setInnerConeAngle(30.0f);
        light.setOuterConeAngle(45.0f);
        mRoot.addChildObject(lightObj);
        mRoot.addChildObject(mCube);
        mRoot.addChildObject(mSphere);
    }

    @Test
    public void spotLightAtFrontAttenuates() throws TimeoutException
    {
        GVRLight.setDefaultQuality(2);
        spotAttenuation();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "spotLightAtFrontAttenuates", mWaiter, mDoCompare);
    }

    @Test
    public void vertexSpotAttenuates() throws TimeoutException
    {
        GVRLight.setDefaultQuality(1);
        spotAttenuation();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "vertexSpotAttenuates", mWaiter, mDoCompare);
    }

    @Test
    public void directLightRotatedIlluminates() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRDirectLight light = new GVRDirectLight(ctx);

        lightObj.getTransform().rotateByAxis(-45, 0, 1, 0);
        lightObj.attachComponent(light);
        mRoot.addChildObject(lightObj);
        mRoot.addChildObject(mCube);
        mRoot.addChildObject(mSphere);
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "directLightRotatedIlluminates", mWaiter, mDoCompare);
    }

    public void directColor() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRDirectLight light = new GVRDirectLight(ctx);

        light.setAmbientIntensity(0.3f, 0.1f, 0.1f, 1);
        light.setDiffuseIntensity(0.3f, 0.3f, 0.6f, 1);
        lightObj.getTransform().rotateByAxis(-45, 0, 1, 0);
        lightObj.attachComponent(light);
        mRoot.addChildObject(lightObj);
    }

    @Test
    public void directLightIlluminatesInColor() throws TimeoutException
    {
        GVRLight.setDefaultQuality(2);
        directColor();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "directLightIlluminatesInColor", mWaiter, mDoCompare);
    }

    @Test
    public void vertexDirectColor() throws TimeoutException
    {
        GVRLight.setDefaultQuality(1);
        directColor();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "vertexDirectColor", mWaiter, mDoCompare);
    }

    public void directSpecular() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj = new GVRSceneObject(ctx);
        GVRDirectLight light = new GVRDirectLight(ctx);

        lightObj.getTransform().rotateByAxis(-45, 0, 1, 0);
        lightObj.attachComponent(light);
        mSphere.getRenderData().getMaterial().setSpecularColor(0.8f, 0.8f, 0.8f, 1.0f);
        mSphere.getRenderData().getMaterial().setSpecularExponent(8.0f);
        mRoot.addChildObject(lightObj);
        mRoot.addChildObject(mCube);
        mRoot.addChildObject(mSphere);
    }

    @Test
    public void directLightHasSpecularReflection() throws TimeoutException
    {
        GVRLight.setDefaultQuality(2);
        directSpecular();
        GVRContext ctx  = mTestUtils.getGvrContext();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "directLightHasSpecularReflection", mWaiter, mDoCompare);
    }

    @Test
    public void vertexDirectSpecular() throws TimeoutException
    {
        GVRLight.setDefaultQuality(1);
        directSpecular();
        GVRContext ctx  = mTestUtils.getGvrContext();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "vertexDirectSpecular", mWaiter, mDoCompare);
    }

    public void directPoint() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj1 = new GVRSceneObject(ctx);
        GVRDirectLight light1 = new GVRDirectLight(ctx);
        GVRSceneObject lightObj2 = new GVRSceneObject(ctx);
        GVRPointLight light2 = new GVRPointLight(ctx);

        light1.setDiffuseIntensity(1, 0, 0.5f, 1);
        light2.setDiffuseIntensity(0, 0.2f, 0.5f, 1);
        lightObj1.getTransform().rotateByAxis(-90, 1, 0, 0);
        lightObj2.getTransform().setPositionZ(4);
        lightObj1.attachComponent(light1);
        lightObj2.attachComponent(light2);
        mSphere.getRenderData().getMaterial().setDiffuseColor(1, 1, 1, 1);
        mRoot.addChildObject(lightObj1);
        mRoot.addChildObject(lightObj2);
        mRoot.addChildObject(mSphere);
    }

    @Test
    public void directAndPointLightsIlluminate() throws TimeoutException
    {
        GVRLight.setDefaultQuality(2);
        directPoint();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "directAndPointLightsIlluminate", mWaiter, mDoCompare);
    }

    @Test
    public void vertexDirectPoint() throws TimeoutException
    {
        GVRLight.setDefaultQuality(1);
        directPoint();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "directAndPointLightsIlluminate", mWaiter, mDoCompare);
    }

    @Test
    public void canDisableLight() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj1 = new GVRSceneObject(ctx);
        GVRDirectLight light1 = new GVRDirectLight(ctx);
        GVRSceneObject lightObj2 = new GVRSceneObject(ctx);
        GVRPointLight light2 = new GVRPointLight(ctx);

        light1.setDiffuseIntensity(1, 0, 0.5f, 1);
        light2.setDiffuseIntensity(0, 0.2f, 0.5f, 1);
        lightObj1.getTransform().rotateByAxis(-90, 1, 0, 0);
        lightObj2.getTransform().setPositionZ(4);
        lightObj1.attachComponent(light1);
        lightObj2.attachComponent(light2);
        mSphere.getRenderData().getMaterial().setDiffuseColor(1, 1, 1, 1);
        mRoot.addChildObject(lightObj1);
        mRoot.addChildObject(lightObj2);
        mRoot.addChildObject(mSphere);
        mTestUtils.waitForXFrames(2);
        light2.setEnable(false);
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "canDisableLight", mWaiter, mDoCompare);
    }

    @Test
    public void canEnableLight() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj1 = new GVRSceneObject(ctx);
        GVRDirectLight light1 = new GVRDirectLight(ctx);
        GVRSceneObject lightObj2 = new GVRSceneObject(ctx);
        GVRPointLight light2 = new GVRPointLight(ctx);

        light1.setEnable(false);
        light1.setDiffuseIntensity(1, 0, 0.5f, 1);
        light2.setDiffuseIntensity(0, 0.2f, 0.5f, 1);
        lightObj1.getTransform().rotateByAxis(-90, 1, 0, 0);
        lightObj2.getTransform().setPositionZ(4);
        lightObj1.attachComponent(light1);
        lightObj2.attachComponent(light2);
        mSphere.getRenderData().getMaterial().setDiffuseColor(1, 1, 1, 1);
        mRoot.addChildObject(lightObj1);
        mRoot.addChildObject(lightObj2);
        mRoot.addChildObject(mSphere);
        mTestUtils.waitForXFrames(2);
        light1.setEnable(true);
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "directAndPointLightsIlluminate", mWaiter, mDoCompare);
    }

    public void spot2() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj1 = new GVRSceneObject(ctx);
        GVRSpotLight light1 = new GVRSpotLight(ctx);
        GVRSceneObject lightObj2 = new GVRSceneObject(ctx);
        GVRSpotLight light2 = new GVRSpotLight(ctx);

        light1.setInnerConeAngle(20.0f);
        light1.setOuterConeAngle(30.0f);
        light1.setDiffuseIntensity(1.0f, 0.3f, 0.3f, 1.0f);
        light2.setInnerConeAngle(10.0f);
        light2.setOuterConeAngle(20.0f);
        light2.setDiffuseIntensity(0.3f, 1.0f, 0.3f, 1.0f);
        lightObj1.getTransform().rotateByAxis(-45, 0, 1, 0);
        lightObj1.getTransform().setPosition(-1, 0, 1);
        lightObj2.getTransform().rotateByAxis(45, 0, 1, 0);
        lightObj2.getTransform().setPosition(2, 0, 2);
        lightObj1.attachComponent(light1);
        lightObj2.attachComponent(light2);
        mSphere.getRenderData().getMaterial().setDiffuseColor(0.8f, 0.8f, 0.8f, 1.0f);
        mSphere.getRenderData().getMaterial().setSpecularColor(0.8f, 0.8f, 0.8f, 1.0f);
        mSphere.getRenderData().getMaterial().setSpecularExponent(8.0f);
        mRoot.addChildObject(lightObj1);
        mRoot.addChildObject(lightObj2);
        mRoot.addChildObject(mCube);
        mRoot.addChildObject(mSphere);
    }

    @Test
    public void twoSpotLightsIlluminate() throws TimeoutException
    {
        GVRLight.setDefaultQuality(2);
        spot2();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "twoSpotLightsIlluminate", mWaiter, mDoCompare);
    }

    @Test
    public void twoVertexSpotLights() throws TimeoutException
    {
        GVRLight.setDefaultQuality(1);
        spot2();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "twoVertexSpotLights", mWaiter, mDoCompare);
    }

    public void pointSpot2() throws TimeoutException
    {
        GVRContext ctx  = mTestUtils.getGvrContext();
        GVRSceneObject lightObj1 = new GVRSceneObject(ctx);
        GVRSpotLight light1 = new GVRSpotLight(ctx);
        GVRSceneObject lightObj2 = new GVRSceneObject(ctx);
        GVRSpotLight light2 = new GVRSpotLight(ctx);
        GVRSceneObject lightObj3 = new GVRSceneObject(ctx);
        GVRPointLight light3 = new GVRPointLight(ctx);

        light1.setInnerConeAngle(20.0f);
        light1.setOuterConeAngle(30.0f);
        light1.setDiffuseIntensity(1.0f, 0.3f, 0.3f, 1.0f);
        light2.setInnerConeAngle(10.0f);
        light2.setOuterConeAngle(20.0f);
        light2.setDiffuseIntensity(0.3f, 1.0f, 0.3f, 1.0f);
        light3.setFloat("radius", 1.5f);
        light3.setDiffuseIntensity(0, 0, 1, 1.0f);
        lightObj1.setName("light1");
        lightObj2.setName("light2");
        lightObj3.setName("light3");
        lightObj1.getTransform().rotateByAxis(-45, 0, 1, 0);
        lightObj1.getTransform().setPosition(-1, 0, 1);
        lightObj2.getTransform().rotateByAxis(45, 0, 1, 0);
        lightObj2.getTransform().setPosition(2, 0, 2);
        lightObj3.getTransform().setPosition(0, 0.5f, -1);
        lightObj3.attachComponent(light3);
        lightObj1.attachComponent(light1);
        lightObj2.attachComponent(light2);
        mSphere.getRenderData().getMaterial().setDiffuseColor(0.8f, 0.8f, 0.8f, 1.0f);
        mSphere.getRenderData().getMaterial().setSpecularColor(0.8f, 0.8f, 0.8f, 1.0f);
        mSphere.getRenderData().getMaterial().setSpecularExponent(8.0f);
        mRoot.addChildObject(lightObj1);
        mRoot.addChildObject(lightObj2);
        mRoot.addChildObject(lightObj3);
        mRoot.addChildObject(mCube);
        mRoot.addChildObject(mSphere);
    }

    @Test
    public void threePixelLights() throws TimeoutException
    {
        GVRLight.setDefaultQuality(2);
        pointSpot2();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "threePixelLights", mWaiter, mDoCompare);
    }

    @Test
    public void threeVertexLights() throws TimeoutException
    {
        GVRLight.setDefaultQuality(1);
        pointSpot2();
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "threeVertexLights", mWaiter, mDoCompare);
    }

    @Test
    public void vertexPointPixelSpot2() throws TimeoutException
    {
        GVRLight.setDefaultQuality(2);
        pointSpot2();
        GVRSceneObject sceneObj = mTestUtils.getMainScene().getSceneObjectByName("light3");
        GVRLight light = (GVRLight) sceneObj.getComponent(GVRLight.getComponentType());
        light.setQuality(1);
        mTestUtils.waitForXFrames(2);
        mTestUtils.screenShot(getClass().getSimpleName(), "vertexPointPixelSpot2", mWaiter, mDoCompare);
    }
}
