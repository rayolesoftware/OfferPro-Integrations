module.exports = {
  dependency: { platforms: { android: {
    sourceDir: './android',
    packageImportPath: 'import com.offerpro.reactnative.OfferProSdkPackage;',
    packageInstance: 'new OfferProSdkPackage()',
  }, ios: null } },
};
