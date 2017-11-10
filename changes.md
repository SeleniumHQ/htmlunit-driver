Change log
==========

2.28 (2017-11-12)
-------------------

### Changed

- Update Selenium to 3.7.0
- Update HtmlUnit to 2.28

### Fixed

- Handle null argument in sendKeys (#52)


2.27 (2017-04-04)
-------------------

### Changed

- Update HtmlUnit to 2.27
- Update Selenium to 3.4.0

### Fixed

- element.getAttribute now searches the hierarchy


2.26 (2017-04-04)
-------------------

### Changed

- Update HtmlUnit to 2.26
- sendKeys() now works asynchronously
- navigate() now works asynchronously
- doubleClick() now works asynchronously
- mouse up/down/move now works asynchronously
- support element.getAttribute("outerHTML"); (#45)
- support element.getAttribute("innerHTML"); (#45)

### Fixed

- finding elements by XPath in XML Documents
- avoid class cast exception when clicking on SVG elements
- fix clicking on invisible elements

