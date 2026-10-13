#version 150

in vec3 vPos;

uniform mat4 CamRot;
uniform float Time;

out vec4 fragColor;

// ============ 噪声函数 ============
// 3D hash：输入任意 vec3，输出 [0,1) 的伪随机值
float hash31(vec3 p) {
    p = fract(p * vec3(443.897, 441.423, 437.195));
    p += dot(p, p.yzx + 19.19);
    return fract((p.x + p.y) * p.z);
}

// 用 hash 判断"这个方向是不是有星星"
float starField(vec3 dir) {
    // 把方向投影到一个格子空间
    // 格子越大星星越稀疏
    vec3 cellPos = dir * 150.0;
    vec3 cellId = floor(cellPos);
    vec3 cellLocal = fract(cellPos);

    // 每个格子里随机决定是否有星星
    float rnd = hash31(cellId);
    if (rnd < 0.985) return 0.0;   // 98.5% 的格子没有星星

    // 星星在格子里的随机位置
    vec3 starPos = vec3(
        hash31(cellId + 1.0),
        hash31(cellId + 2.0),
        hash31(cellId + 3.0)
    );
    // 星星位置映射到格子中心附近
    starPos = 0.5 + (starPos - 0.5) * 0.8;

    // 计算像素到星星的距离
    float d = length(cellLocal - starPos);

    // 亮度：距离越近越亮
    float brightness = smoothstep(0.15, 0.0, d);

    // 星星的闪烁
    float twinkle = 0.7 + 0.3 * sin(Time * 2.0 + hash31(cellId) * 100.0);

    return brightness * twinkle;
}

void main() {
    // vPos 是当前像素的方向（四边形顶点的方向，插值得到）
    vec3 dir = normalize(vPos);

    // 用相机旋转矩阵把"世界方向"转到"相机方向"
    // 但这里我们反过来：世界空间的星星是固定的，不需要跟相机转
    // 直接用世界方向 dir 采样即可
    vec3 worldDir = normalize((inverse(CamRot) * vec4(dir, 0.0)).xyz);

    // 采样星星场
    float star = starField(worldDir);

    // 星星颜色：微微偏蓝的白色
    vec3 starColor = vec3(0.9, 0.95, 1.0) * star;

    // 背景是纯黑
    fragColor = vec4(starColor, 1.0);
}