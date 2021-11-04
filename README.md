# 혼자 진행하는 SpringBoot


### 환경설정

---

- <span style="color:#ffff">mariaDB를 사용</span>

- <span style="color:#ffff">application.yml 설정</span>


~~~ yml
spring:
  # DataBase 설정
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://<서버주소:port>
    username: <사용자 ID>
    password: <사용자 Password>
    initialization-mode: always

  # JPA 설정
  jpa:
    hibernate:
      ddl-auto: validation
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDB102Dialect
#        show_sql: true
        format_sql: true

  # 자동 빌드
  devtools:
    livereload:
      enabled: true

# Logging 설정
logging.level:
    org.hibernate.SQL: debug
#    org.hibernate.type: trace


# Server Port
server:
  port: 8080

~~~

- <span style="color:#ffff">application-jwt.yml 설정</span>

~~~ yml
app:
  auth:
    token-secret: <시크릿>
    # Access Token 만료기간 
    access-token-expiration-msec: 7200000
    # Refresh Token 만료기간
    refresh-token-expiration-msec: 864000000

  # OAuth2 RedirectURI  
  o-auth2:
    authorized-redirect-uris: http://localhost:3000/oauth2/redirect
~~~

Aws S3 Image Resize 기능 구현 추가 예정
