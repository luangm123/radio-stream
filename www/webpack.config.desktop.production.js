/* eslint strict: 0 */
'use strict';

const webpack = require('webpack');
const webpackTargetElectronRenderer = require('webpack-target-electron-renderer');
const baseConfig = require('./webpack.config.base.production');

const config = Object.create(baseConfig);

config.plugins.push(
  new webpack.DefinePlugin({
    '__WEB__': false
  })
);

config.target = webpackTargetElectronRenderer(config);

module.exports = config;
