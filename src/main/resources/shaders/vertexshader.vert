#version 130

#if __VERSION__ >= 130
  #define attribute in
  #define varying out
#endif

attribute vec3 inVertex;
attribute vec3 inNormal;

uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;

varying vec3 normal;
varying vec3 fragmentPos;

void main(void)
{
//    vertexFrag = vec3(modelviewMatrix * vec4(inVertex,1f));
//    model = modelviewMatrix;
    normal = vec3(modelMatrix * vec4(inNormal, 0));
    fragmentPos = vec3(modelMatrix * vec4(inVertex, 0));
    gl_Position = projectionMatrix * (viewMatrix * (modelMatrix * vec4(inVertex,1.0)));
}