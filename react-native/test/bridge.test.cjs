const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const source = fs.readFileSync(require('node:path').join(__dirname, '../index.js'), 'utf8')
  .replace(/^import .*;\n/m, '').replace('export const OfferPro', 'const OfferPro')
  .replace('export default OfferPro;', 'globalThis.api = OfferPro;');
function setup(os, module) {
  const context = { NativeModules: { OfferProSdk: module }, Platform: { OS: os } };
  vm.createContext(context); vm.runInContext(source, context); return context.api;
}
(async () => {
  const calls = [];
  const native = new Proxy({}, { get: (_, method) => (...args) => { calls.push({method, args}); return Promise.resolve(); } });
  const api = setup('android', native);
  await api.getUsageTimeMs('com.example.app', 100, 200);
  await api.validateAppUsage('com.example.app', 100, 200);
  assert.deepEqual(JSON.parse(JSON.stringify(calls)), [
    {method: 'getUsageTimeMs', args: ['com.example.app', 100, 200]},
    {method: 'validateAppUsage', args: ['com.example.app', 100, 200]},
  ]);
  await assert.rejects(setup('ios', native).showOfferPro(), /Android only/);
  await assert.rejects(setup('android', undefined).showOfferPro(), /native module is missing/);
  console.log('React Native bridge contract checks passed');
})().catch(error => { console.error(error); process.exitCode = 1; });
