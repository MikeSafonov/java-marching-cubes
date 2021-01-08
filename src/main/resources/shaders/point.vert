#version 130

#if __VERSION__ >= 130
  #define attribute in
  #define varying out
#endif

attribute vec3 position;

uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;


void main() {
    gl_PointSize = 2.0;
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position,1.0);

}