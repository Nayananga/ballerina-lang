const boolean someBoolean = 10;
const int someInt = "ABC";
const byte someByte = 500;
const float someFloat = true;
const string someString = 120;

// Assigning var ref.
string s = "Ballerina";
const string name = s;
public const string name2 = s;

// Assigning var ref.
int a = 10;
const age = a;
public const age2 = a;

// Updating const.
const x = 10;
const int y = 20;

function testAssignment() {
    x = 1;
    y = 2;
}

// Updating const in worker.
function testWorkerInteractions() {
    worker w1 {
        x <- w2;
    }
    worker w2 {
        30 -> w1;
    }
}

const string sVar = 10;

const string m = { name: "Ballerina" };

// Redeclared constant.
const abc = "abc";

const abc = "Ballerina";

// Redeclared variable.
const def = "def";

function test() {
    string def = "def";
}

// Incompatible types.
type ACTION "GET";

type XYZ "XYZ";

const XYZ xyz = "XYZ";

function testInvalidTypes() returns ACTION {
    ACTION action = xyz;
    return action;
}

// Built-in function invocation.
function testInvalidInvocation() {
    string lowercase = xyz.toLower();
}

const aBoolean = true;

function testBooleanConcat() returns string {
    return aBoolean + " rocks";
}

const aInt = 24;

function testIntConcat() returns string {
    return aInt + " rocks";
}

const aByte = 12;

function testByteConcat() returns string {
    return aByte + " rocks";
}

const aFloat = 25.5;

function testFloatConcat() returns string {
    return aFloat + " rocks";
}

const name = "Ballerina";

function testConstConcat() returns string {
    return name + " rocks";
}

// -----------------------------------------------------------

const conditionWithoutType = true;

function testConstWithoutTypeInCondition() returns boolean {
    if (conditionWithoutType) {
        return true;
    }
    return false;
}

// -----------------------------------------------------------

const booleanWithoutType = true;

function testBooleanWithoutType() returns boolean {
    return booleanWithoutType;
}

const intWithoutType = 20;

function testIntWithoutType() returns int {
    return intWithoutType;
}

const byteWithoutType = 120;

function testByteWithoutType() returns byte {
    return byteWithoutType;
}

const floatWithoutType = 2.0;

function testFloatWithoutType() returns float {
    return floatWithoutType;
}

const stringWithoutType = "Ballerina rocks";

function testStringWithoutType() returns string {
    return stringWithoutType;
}

// -----------------------------------------------------------

const D = "D";

const E = "E";

const F = "F";

type G E|F;

type H D|E;

const H h = "D";

function testImproperSubset() returns G {
    G g = h;
    return g;
}
