#/bin/sh!

set +u

BUILD_DIR=`dirname "$0"` || {
    echo "'dirname' failed" 1>&2
    exit 2
}

BUILD_PROG=`which xbuild` ||  {
    echo "No xbuild found on path: ${PATH}"
    echo "Is mono installed?"
    exit 2
}


pushd `pwd`

cd "$BUILD_DIR"

"${BUILD_PROG}" /t:Build "System.Data.Hsqldb.vs2008.sln"

popd