
### spring-webflux-security
#### [博客](https://my.oschina.net/u/3876288/blog/3134512)
### spring-feign
#### [多model入参](https://my.oschina.net/u/3876288/blog/3149175)
### spring-web 

#### [多model入参](https://my.oschina.net/u/3876288/blog/3206995)

 `启动项目在linux、macos终端执行 或者postman执行这面接口及参数`
```shell

curl -X POST \
  http://127.0.0.1:9100/quryUserDeptByModels \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: 35bcd637-3c7a-487d-b546-8da094be75ff' \
  -H 'cache-control: no-cache' \
  -d '{
	"user": {
		"userId": "userid",
		"userName": "username"		
	},
	"dept": {
		"deptId": "deptid",
		"deptName": "deptname"		
	},
	"userArray":[
		 {
			"userId": "userid",
			"userName": "username"		
		}
	],
	"deptList":[
		 {
			"deptId": "deptid",
			"deptName": "deptname"	
		}
	]
}'
```
