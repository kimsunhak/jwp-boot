# 혼자 진행하는 SpringBoot

---

## 환경설정

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