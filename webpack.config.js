const path = require("path");
const NodePolyfillPlugin = require("node-polyfill-webpack-plugin");
const TerserPlugin = require("terser-webpack-plugin");

const production = {
  name: "production",
  entry: {
          app: "./target/app_libs.js"
          },
  output: {
    path: path.resolve(__dirname, "project/resources/public/js/libs"),
    filename: '[name].js',
    clean: true
  },
  
  optimization: {
    minimize: true,
    minimizer: [new TerserPlugin({terserOptions: 
                                  { 
                                   compress: {dead_code:    true,
                                              drop_console: true},
                                   output:   {comments:    false,
                                              beautify:    false,
                                              ascii_only:  true,
                                              quote_style: 1,
                                              semicolons:  false}}})]
  },

  // plugins: [new NodePolyfillPlugin()],

  module: {
    rules: [
      {
        test: /\.(js|jsx)$/i,
        use: {
          loader: 'babel-loader',
        },
      },
      {
        test: /\.(eot|svg|ttf|woff|woff2|png|jpg|gif)$/i,
        type: "asset",
      },
      {
        test: /\.mjs$/,
        include: /node_modules/,
        type: "javascript/auto"
      }

      // {
      //   test: /\.m?js/,
      //   resolve: {
      //     fullySpecified: false,
      //   },
      // },

    ],
  },

};

const dev_output = {
  path: path.resolve(__dirname, "project/resources/public/js/libs"),
  filename: '[name].js',
  // No clean, so multiple build can be built.
  //clean: true
}

const app = {
  name: "app",
  entry: {
          app: "./target/app_libs.js",
          },
  output: dev_output};

module.exports = [production, app];
