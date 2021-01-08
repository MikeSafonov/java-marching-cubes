#version 130

#if __VERSION__ >= 130
  #define attribute in
  #define varying out
#endif
//in vec3 vertexFrag;
//in mat4 model;
attribute vec3 normal;
attribute vec3 fragmentPos;

varying vec4 outColor;

uniform float ambientStrength = 0.1;
uniform float diffuseIntensity = .7;
uniform vec3 objectColor = vec3(1.0,1.0,1.0);
uniform vec3 lightColor = vec3(1.0,1.0,1.0);
uniform vec3 direction = vec3(0.0,0.0,-3.0);
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform float materialSpecularFactor = 1.0;
uniform float specularIntensity = .8f;
uniform float materialSpecularIntensity = .8;
uniform vec3 lightPos = vec3(1.0,1.0,1.0);

void main(void)
{

    vec3 normalizedNormale = normalize(normal);
    vec3 ambient = ambientStrength * lightColor;
    float diffuseFactor = max(0.0, dot(normalizedNormale, -normalize(direction)));
    vec4 diffuseColor = vec4(lightColor, 1) * diffuseIntensity
                          * diffuseFactor;

    vec3 fragmentToCamera = normalize(vec3(viewMatrix) - fragmentPos);

    vec3 lightReflect = normalize(reflect(direction, normal));
    float specularFactor = pow(max(0.0, dot(fragmentToCamera, lightReflect)), materialSpecularFactor);
    vec4 specularColor = specularIntensity * vec4(lightColor, 1) * materialSpecularIntensity * specularFactor;

    outColor = vec4(ambient,1.0) + diffuseColor + specularColor;



}
