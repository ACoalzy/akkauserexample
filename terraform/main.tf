variable "login" {}
variable "image" {}
variable "key" {}

provider "aws" {
  region = "eu-west-1"
}

resource "aws_instance" "example" {
  ami = "ami-a61464df"
  instance_type = "t2.micro"
  key_name = "userapi"

  tags {
    Name = "terraform-example"
  }

  vpc_security_group_ids = ["${aws_security_group.instance.id}"]

  connection {
    user = "core"
    private_key = "${file("${var.key}")}"
  }

  provisioner "remote-exec" {
    inline = [
      "${var.login}",
      "docker pull ${var.image}",
      "docker run --rm -d -p8080:8080 4ce757029c74"
    ]
  }
}

resource "aws_security_group" "instance" {
  name = "terraform-example-instance"
  ingress {
    from_port = 8080
    to_port = 8080
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port       = 0
    to_port         = 0
    protocol        = "-1"
    cidr_blocks     = ["0.0.0.0/0"]
  }
 
}

output "public_ip" {
  value = "${aws_instance.example.public_ip}"
}
