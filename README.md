
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


#### spring-webmvc-sercurity

`resetfulApi`
```shell
    
    登录接口
        curl -X POST \
          http://127.0.0.1:9101/loginJson \
          -H 'Content-Type: application/json' \
          -H 'Postman-Token: cb5d5c24-1ffc-4f8b-8ec5-3641942117d0' \
          -H 'cache-control: no-cache' \
          -d '{
        	"username" : "user",
        	"passwd": "password"
        }'
        
        或者使用
        curl -X POST \
          http://127.0.0.1:9101/login \
          -H 'Content-Type: application/x-www-form-urlencoded' \
          -H 'Postman-Token: 537f7268-9093-44a3-b258-807414a1bcd2' \
          -H 'cache-control: no-cache' \
          -d 'userName=user&passwd=password'

    登录接口返回 
        {
            "token": "f4fb2820dd3048948020264aaed1a8ad"
        }
     
    调用其他接口
        token不存在
        curl -X GET \
          http://127.0.0.1:9101/user/queryUser \
          -H 'Postman-Token: 236fb1e2-5f70-4263-affc-c377166dd8a4' \
          -H 'cache-control: no-cache'
          
        使用token
        Authorization: Bearer ${token}
        Authorization: Bearer f4fb2820dd3048948020264aaed1a8ad
        curl -X GET \
          http://127.0.0.1:9101/user/queryUser \
          -H 'Authorization: Bearer f4fb2820dd3048948020264aaed1a8ad' \
          -H 'Postman-Token: 9defd035-facb-4538-a526-a5b23a936613' \
          -H 'cache-control: no-cache'
          
        403
        curl -X GET \
          http://127.0.0.1:9101/dept/queryDept \
          -H 'Authorization: Bearer f4fb2820dd3048948020264aaed1a8ad' \
          -H 'Postman-Token: 60385c46-a322-4d33-988c-3fac07215010' \
          -H 'cache-control: no-cache' \
          -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
          -F message=message
```
