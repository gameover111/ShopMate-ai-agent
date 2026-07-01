# 使用 Nginx 镜像来 serve 静态文件
FROM nginx:alpine

# 删除 Nginx 默认的静态文件
RUN rm -rf /usr/share/nginx/html/*

# 复制你自己构建好的 dist 文件夹到 Nginx 的静态目录
COPY dist /usr/share/nginx/html

# 复制你自定义的 Nginx 配置文件
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露 80 端口
EXPOSE 80

# Nginx 会自动启动，不需要额外 CMD