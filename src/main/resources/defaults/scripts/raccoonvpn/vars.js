document.raccoon={
	protocol: "{{protocol}}",
	port: "{{port}}",
	v: "{{version}}"
}
document.raccoon.endpoint = location.protocol+"//"+window.location.hostname+":"+location.port+"/api/"+document.raccoon.v+"/";