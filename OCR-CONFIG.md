# OCR配置说明

本项目支持两种OCR识别模式：本地OCR和云端OCR服务。

## 配置方式

### 1. 环境变量配置

复制 `.env.example` 为 `.env` 文件，然后配置相应的环境变量：

```bash
cp .env.example .env
```

### 2. application.yml配置

在 `src/main/resources/application.yml` 中配置OCR相关参数：

```yaml
ocr:
  # OCR模式: local(本地Tesseract) 或 cloud(云端服务)
  mode: local
  # 云端服务商: aliyun(阿里云), baidu(百度云), tencent(腾讯云)
  cloud-provider: aliyun
  # 本地OCR配置
  local:
    tesseract-path: C:/Program Files/Tesseract-OCR/tessdata
    language: chi_sim
  # 阿里云OCR配置
  aliyun:
    access-key-id: ${ALIYUN_ACCESS_KEY_ID:}
    access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET:}
    endpoint: ocr-api.cn-hangzhou.aliyuncs.com
  # 百度云OCR配置
  baidu:
    app-id: ${BAIDU_APP_ID:}
    api-key: ${BAIDU_API_KEY:}
    secret-key: ${BAIDU_SECRET_KEY:}
  # 腾讯云OCR配置
  tencent:
    secret-id: ${TENCENT_SECRET_ID:}
    secret-key: ${TENCENT_SECRET_KEY:}
    region: ap-guangzhou
```

## 支持的云端OCR服务

### 1. 阿里云OCR

**申请步骤：**
1. 登录[阿里云控制台](https://console.aliyun.com/)
2. 开通"文字识别OCR"服务
3. 创建AccessKey
4. 获取AccessKey ID和AccessKey Secret

**配置示例：**
```yaml
ocr:
  mode: cloud
  cloud-provider: aliyun
  aliyun:
    access-key-id: LTAI5t****
    access-key-secret: ****
```

### 2. 百度云OCR

**申请步骤：**
1. 登录[百度智能云控制台](https://console.bce.baidu.com/)
2. 创建文字识别应用
3. 获取AppID、API Key和Secret Key

**配置示例：**
```yaml
ocr:
  mode: cloud
  cloud-provider: baidu
  baidu:
    app-id: 12345678
    api-key: ****
    secret-key: ****
```

### 3. 腾讯云OCR

**申请步骤：**
1. 登录[腾讯云控制台](https://console.cloud.tencent.com/)
2. 开通"文字识别"服务
3. 创建API密钥
4. 获取SecretId和SecretKey

**配置示例：**
```yaml
ocr:
  mode: cloud
  cloud-provider: tencent
  tencent:
    secret-id: AKID****
    secret-key: ****
```

## API接口

### 获取OCR配置信息

```
GET /api/ocr/config
```

**响应示例：**
```json
{
  "mode": "cloud",
  "currentProvider": "aliyun",
  "availableCloudServices": ["aliyun", "baidu", "tencent"]
}
```

### 获取可用的云端服务列表

```
GET /api/ocr/providers
```

**响应示例：**
```json
["aliyun", "baidu", "tencent"]
```

## 使用说明

1. **本地OCR模式**：需要安装Tesseract OCR软件，并配置正确的路径
2. **云端OCR模式**：需要配置相应的云服务密钥
3. **切换模式**：修改 `ocr.mode` 配置项即可切换
4. **切换服务商**：修改 `ocr.cloud-provider` 配置项即可切换

## 注意事项

1. 云端OCR服务需要网络连接
2. 云端OCR服务可能产生费用，请参考各云服务商的计费标准
3. 本地OCR需要安装Tesseract OCR软件
4. 建议在生产环境使用云端OCR服务，以获得更好的识别效果
5. 开发环境可以使用本地OCR服务，节省成本