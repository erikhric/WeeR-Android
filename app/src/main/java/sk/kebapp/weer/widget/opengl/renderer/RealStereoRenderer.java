package sk.kebapp.weer.widget.opengl.renderer;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by erikhric on 01/08/16.
 */
public class RealStereoRenderer extends CubeGLRenderer {

    public RealStereoRenderer(Context context) {
        super(context);
        mContext = context;
        final float[] cubePositionData =
                {
                        // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
                        // if the points are counter-clockwise we are looking at the "front". If not we are looking at
                        // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
                        // usually represent the backside of an object and aren't visible anyways.

                        // Front face
//                        -1.0f, 1.0f, 1.0f,
//                        -1.0f, -1.0f, 1.0f,
//                        1.0f, 1.0f, 1.0f,
//                        -1.0f, -1.0f, 1.0f,
//                        1.0f, -1.0f, 1.0f,
//                        1.0f, 1.0f, 1.0f,
//
//                        // Right face
//                        1.0f, 1.0f, 1.0f,
//                        1.0f, -1.0f, 1.0f,
//                        3.0f, 1.0f, 1.0f,
//                        1.0f, -1.0f, 1.0f,
//                        3.0f, -1.0f, 1.0f,
//                        3.0f, 1.0f, 1.0f,

//scaled up
                        -3.0f, 2.0f, 1.0f,
                        -3.0f, -2.0f, 1.0f,
                        1.0f, 2.0f, 1.0f,
                        -3.0f, -2.0f, 1.0f,
                        1.0f, -2.0f, 1.0f,
                        1.0f, 2.0f, 1.0f,

                        // Right face
                        1.0f, 2.0f, 1.0f,
                        1.0f, -2.0f, 1.0f,
                        5.0f, 2.0f, 1.0f,
                        1.0f, -2.0f, 1.0f,
                        5.0f, -2.0f, 1.0f,
                        5.0f, 2.0f, 1.0f,
                };

        // R, G, B, A
        final float[] cubeColorData =
                {
                        // Front face (red)
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,

                        // Right face (green)
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                };

        // X, Y, Z
        // The normal is used in light calculations and is a vector which points
        // orthogonal to the plane of the mSurface. For a cube model, the normals
        // should be orthogonal to the points of each face.
        final float[] cubeNormalData =
                {
                        // Front face
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,

                        // Right face
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                };

        // S, T (or X, Y)
        // Texture coordinate data.
        // Because images have a Y axis pointing downward (values increase as you move down the image) while
        // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
        // What's more is that the texture coordinates are the same for every face.
        final float[] cubeTextureCoordinateData =
                {
                        // Front face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        0.5f, 0.0f,
                        0.0f, 1.0f,
                        0.5f, 1.0f,
                        0.5f, 0.0f,

//                         Right face
                        0.5f, 0.0f,
                        0.5f, 1.0f,
                        1.0f, 0.0f,
                        0.5f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,
                };

        // Initialize the buffers.
        mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePositionData).position(0);

        mCubeColors = ByteBuffer.allocateDirect(cubeColorData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColors.put(cubeColorData).position(0);

        mCubeNormals = ByteBuffer.allocateDirect(cubeNormalData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals.put(cubeNormalData).position(0);

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);

    }
}
