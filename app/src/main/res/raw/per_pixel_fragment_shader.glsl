// important to include in order to use rendered Android View to gl texture
#extension GL_OES_EGL_image_external : require

//make sure to use samplerExternalOES instead of sampler2D
uniform samplerExternalOES u_Texture;    // The input texture.

precision mediump float;       	// Set the default precision to medium. We don't need as high of a
								// precision in the fragment shader.
uniform vec3 u_LightPos;       	// The position of the light in eye space.


varying vec3 v_Position;		// Interpolated position for this fragment.
varying vec4 v_Color;          	// This is the color from the vertex shader interpolated across the 
  								// triangle per fragment.
varying vec3 v_Normal;         	// Interpolated normal for this fragment.
varying vec2 v_TexCoordinate;   // Interpolated texture coordinate per fragment.

//uniform sampler2D u_Texture;
vec2 vScale;
const float alpha = float(4.0 * 2.0 + 0.75);

// The entry point for our fragment shader.
void main()                    		
{
    vScale = vec2(0.27, 0.37);
    float bound2 = 0.25 * (vScale.x * vScale.x + vScale.y * vScale.y);
    float bound = sqrt(bound2);
    float radius = 2.15 * bound;//change this to 1,15 to look ok :)
    float radius2 = radius * radius;
    float max_radian = 0.5 * 3.14159265 - atan(alpha / bound * sqrt(radius2 - bound2));
    float factor = bound / max_radian;
    float m_pi_2 = 1.570963;
    vec2 coord = v_TexCoordinate - vec2(0.5, 0.5);
    float dist = length(coord * vScale);
    float radian = m_pi_2 - atan(alpha * sqrt(radius2 - dist * dist), dist);
    float scalar = radian * factor / dist;
    vec2 new_coord = coord * scalar + vec2(0.5, 0.5);
    gl_FragColor = texture2D(u_Texture, v_TexCoordinate);
    //gl_FragColor = texture2D(u_Texture, new_coord);
}
