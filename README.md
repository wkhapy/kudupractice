# kudupractice
build

1.sudo apt-get install autoconf automake curl flex g++ gcc gdb git \
  krb5-admin-server krb5-kdc krb5-user libkrb5-dev libsasl2-dev libsasl2-modules \
  libsasl2-modules-gssapi-mit libssl-dev libtool lsb-release make ntp \
  openjdk-8-jdk openssl patch pkg-config python rsync unzip vim-common
2.unzip kudu package
go to kudu dir

cd kudu
thirdparty/build-if-necessary.sh

mkdir -p build/release
cd build/release
../../thirdparty/installed/common/bin/cmake -DNO_TESTS=1 -DCMAKE_BUILD_TYPE=release ../..
make -j4
  

