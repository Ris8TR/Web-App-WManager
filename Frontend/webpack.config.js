const CopyPlugin = require("copy-webpack-plugin");

module.exports = {
  plugins: [
    new CopyPlugin({
         from: 'node_modules/@mdi/angular-material/mdi.svg',
        to: 'assets/mdi.svg'
      ,
    }),
  ],
};