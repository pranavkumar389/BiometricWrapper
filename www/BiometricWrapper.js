var exec = require('cordova/exec');

function BiometricWrapper() {
}


BiometricWrapper.prototype.activateIris = function (arg0, success, error) {
    exec(success, error, 'BiometricWrapper', 'activateIris', [arg0]);
};

BiometricWrapper.prototype.activateFingerprint = function (arg0, success, error) {
    exec(success, error, 'BiometricWrapper', 'activateFingerprint', [arg0]);
};

var biometricWrapper = new BiometricWrapper();
module.exports = biometricWrapper;
