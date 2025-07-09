config.resolve = config.resolve || {};
config.resolve.fallback = config.resolve.fallback || {};
config.resolve.fallback.path = require.resolve("path-browserify");
config.resolve.fallback.fs = false;
config.resolve.fallback.crypto = false;

const CopyWebpackPlugin = require('copy-webpack-plugin');
const path = require('path');

// Get the directory of the sql.js package
const sqlJsPath = path.dirname(require.resolve('sql.js'));

if (!config.plugins) {
    config.plugins = [];
}
config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            {
                from: path.join(sqlJsPath, 'sql-wasm.wasm'),
                to: '.'
            }
        ]
    })
);
