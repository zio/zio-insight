const jsFile = require('./jsfile.js')

export default {
  input: `${jsFile}`,
  output: {
    file: 'insight.js',
    format: 'iife'
  }
}
