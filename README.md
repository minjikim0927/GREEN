# GREEN
GREEN은 시리얼 포트를 가지고 있는 다수개의 혈압기의 결과를 모니터링하며 출력합니다.  
![Grimi](Grimi.gif)

## 주요 기능
+ NCS, CS 범위를 설정하여 측정값이 해당되면 표시
+ 측정 데이터 암호화
+ 4개의 혈압계 모니터링
+ 정확한 시간기록을 위해 서버와 시간동기
+ 프린트 양식 지정가능

## SW Prerequisites
+ Netbean 8.2 (JAVA FX included)
+ JAVA 1.8

## HW Prerequisites
+ 4 port RxTx serial port hub (Moxa Uport1450 serial hubs) 
+ 혈압계 (RxTx serial port installed) 

## Installation
+ install netbean with java 1.8
+ link external JARs(in a libs folder) at netbean
+ connect Blood pressures with a hub

## Figures
+ Fig1. Network
![network](/green1.png)
+ Fig2. Monitoring
![monitoring](/green2.png)
+ Fig3. Data encryption
![data encryption](/green3.png)
+ Fig4. Printing form
![printing form](/green4.png)

## Contact to developer(s)
Jhyoung lee - jhlee@jbcp.kr
