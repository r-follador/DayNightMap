#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(com.genewarrior.sunlocator.app)

#include "rs_debug.rsh"

float width;
float height;

float alpha;
float delta;
float h_pre;

float static elev(float latitude, float longitude) {
    float H = h_pre + radians(longitude) - alpha;
    H = fmod((H + M_PI),(2 * M_PI)) - M_PI;
    if (H < 0 - M_PI) {
        H += 2 * M_PI;
    }
    float sPhi = sin(radians(latitude));
    float cPhi = sqrt((1 - sPhi * sPhi));
    float sDelta = sin(delta);
    float cDelta = sqrt(1 - sDelta * sDelta);
    float cH = cos(H);

    float sEpsilon0 = sPhi * sDelta + cPhi * cDelta * cH;
    float eP = asin(sEpsilon0) - 4.26e-5 * sqrt(1.0f - sEpsilon0 * sEpsilon0);


    float z = M_PI / 2 - eP;

    return 90.0f - degrees(z);
}

uchar4 __attribute__((kernel)) root(uchar4 in, uint32_t x, uint32_t y) {
    uchar4 nightColor = rsPackColorTo8888(0.1f, 0.1f, 0.1f, 0.85f);
    uchar4 astroColor = rsPackColorTo8888(0.15f, 0.15f, 0.2f, 0.8f);
    uchar4 nautColor = rsPackColorTo8888(0.12f, 0.2f, 0.40f, 0.7f);
    uchar4 civilColor = rsPackColorTo8888(0.20f, 0.35f, 0.53f, 0.6f);
    uchar4 dayColor = rsPackColorTo8888(1.0f, 1.0f, 1.0f, 0.0f); //full transparency


    //conversion from pixel position to lat/lon on plate carée protection
    float longitude = ((360.0f/width)*(float)x)-180.0f;
    float latitude = 90.0f-((180.0f/height)*(float)y);

    float z = elev(latitude, longitude);

    if (z < -18.0f) {
        return nightColor;
    }
    if (z < -12.0f) { //astronomical twilight
        return astroColor;
    }
    if (z < -6.0f) { //nautical twilight
        return nautColor;
    }
    if (z < -0.8333f) { //civil twilight
        return civilColor;
    }
    return dayColor; //anything above -0.833 is day

}