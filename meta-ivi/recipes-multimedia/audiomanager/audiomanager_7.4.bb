SUMMARY = "Genivi AudioManager"
HOMEPAGE = "https://www.genivi.org/"
SECTION = "multimedia"
LICENSE = "MPLv2"
LIC_FILES_CHKSUM = "file://LICENCE;md5=f164349b56ed530a6642e9b9f244eec5"

PR = "r1"

DEPENDS = "common-api-c++-dbus dlt-daemon sqlite3 dbus node-state-manager"

SRCREV = "fe056279dde92475f29a35f8c0c6c5a41dc59a8b"
SRC_URI = " \
    git://github.com/GENIVI/AudioManager.git;protocol=https \
    file://AudioManager.service \
    file://setup_amgr.sh \
    "
S = "${WORKDIR}/git"

inherit gettext cmake pkgconfig systemd

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "AudioManager.service"
SYSTEMD_AUTO_ENABLE = "disable"

OECMAKE_CXX_FLAGS +="-ldl"
EXTRA_OECMAKE = " -DWITH_DBUS_WRAPPER=ON -DWITH_TESTS=OFF"

FILES_${PN} = " \
    ${bindir}/* \
    ${systemd_unitdir}/AudioManager.service \
    ${systemd_unitdir}/scripts/setup_amgr.sh \
    "
FILES_${PN}-dev += " \
    ${libdir}/* \
    "

do_install_append() {
    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        mkdir -p ${D}${systemd_unitdir}/scripts/
        install -m 0755 ${WORKDIR}/setup_amgr.sh ${D}${systemd_unitdir}/scripts/setup_amgr.sh
        install -d ${D}${systemd_unitdir}/system/
        install -m 0644 ${WORKDIR}/AudioManager.service ${D}${systemd_unitdir}/system
    fi

    perl -pi -e 's|set_and_check\(|#set_and_check\(|' ${D}${libdir}/cmake/*/*.cmake

}

# replace function in poky/meta/classes/insane.bbclass
python do_qa_staging() {
    bb.note("[workaround] QA checking staging")

    if not package_qa_check_staged(d.expand('${SYSROOT_DESTDIR}${STAGING_LIBDIR}'), d):
        bb.fatal("QA staging was broken by the package built above")
}
