provider "aws" {
  region = "eu-west-1"
}

resource "aws_instance" "example" {
  ami = "ami-a61464df"
  instance_type = "t2.micro"

  tags {
    Name = "terraform-example"
  }

  vpc_security_group_ids = ["${aws_security_group.instance.id}"]
}

resource "aws_security_group" "instance" {
  name = "terraform-example-instance"
  ingress {
    from_port = 8080
    to_port = 8080
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

output "public_ip" {
  value = "${aws_instance.example.public_ip}"
}
