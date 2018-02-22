var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'CDVYBPushPlugin', 'coolMethod', [arg0]);
};
exports.addSubscribe = function (arg0, success, error) {
    exec(success, error, 'CDVYBPushPlugin', 'addSubscribe', [arg0]);
};
exports.topSkip = function (arg0, success, error) {
    exec(success, error, 'CDVYBPushPlugin', 'topSkip', [arg0]);
};
exports.unSubscribe = function (arg0, success, error) {
    exec(success, error, 'CDVYBPushPlugin', 'unSubscribe', [arg0]);
};
exports.addAlias = function (arg0, success, error) {
    exec(success, error, 'CDVYBPushPlugin', 'addAlias', [arg0]);
};

