SUMMARY = "Admin interface for Linux machines"
DESCRIPTION = "Cockpit makes it easy to administer your GNU/Linux servers via a web browser"

LICENSE = "LGPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=4fbd65380cdd255951079008b364516c"

SRC_URI  = "file://cockpit.pam"
#SRC_URI += "file://poky-aero"
SRC_URI += "https://github.com/cockpit-project/cockpit/releases/download/${PV}/cockpit-${PV}.tar.xz"
SRC_URI[md5sum] = "b6149d33a540cb40d54eb2e8b6596111"
SRC_URI[sha256sum] = "ef211c614d66a413ccc7575ab089e9151779bf8c74ec254a3f32a1067c64cb36"

inherit gettext pkgconfig autotools systemd distro_features_check

REQUIRED_DISTRO_FEATURES = "pam"

EXTRA_AUTORECONF = "-I tools"
EXTRA_OECONF = "--with-cockpit-user=root \
                --with-cockpit-group=root \
                --disable-ssh \
                --disable-doc \
               "

#                --with-branding=default

PACKAGECONFIG ?= ""
PACKAGECONFIG[pcp] = "--enable-pcp,--disable-pcp,pcp"

SYSTEMD_SERVICE_${PN} = "cockpit.socket"

# Avoid warnings "file XXX is owned by uid 1001, which is the same as the user running bitbake. This may be due to host contamination"
INSANE_SKIP_${PN} += "host-user-contaminated"

PACKAGES =+ "${PN}-bridge ${PN}-pcp ${PN}-docker ${PN}-ws ${PN}-system ${PN}-dashboard ${PN}-networkmanager"

FILES_${PN} += "${libdir}/firewalld \
                ${libdir}/security \
                ${libdir}/tmpfiles.d \
                ${datadir}/appdata \
                ${datadir}/metainfo \
                ${datadir}/polkit-1 \
                ${systemd_unitdir}/system/${PN}.socket \
                ${systemd_unitdir}/system/${PN}.service \
                ${systemd_unitdir}/system/${PN}-motd.service \
                "

FILES_${PN}-pcp =+ "${libexecdir}/cockpit-pcp \
                    ${datadir}/cockpit/pcp \
                    ${localstatedir}/lib/pcp/config/pmlogconf/tools/cockpit \
                    "

FILES_${PN}-bridge =+ "${bindir}/cockpit-bridge \
                       ${datadir}/cockpit/base1 \
                       ${libexec}/cockpit-askpass \
                       "
FILES_${PN}-docker =+ "${datadir}/cockpit/docker "

FILES_${PN}-ws =+ "${libexecdir}/cockpit-session \
                   ${libexecdir}/cockpit-ws \
                   ${sbindir}/remotectl \
                   ${datadir}/cockpit/branding \
                   ${datadir}/cockpit/static \
                   "
FILES_${PN}-system =+ "${datadir}/cockpit/systemd \
                       ${datadir}/cockpit/users \
                       ${datadir}/cockpit/shell \
                       "

FILES_${PN}-dashboard =+ "${datadir}/cockpit/dashboard \
                          ${datadir}/cockpit/ssh \
                          ${libexecdir}/cockpit-ssh \
                          "

FILES_${PN}-networkmanager =+ "${datadir}/cockpit/networkmanager"

DEPENDS += "glib-2.0-native intltool-native"
DEPENDS += "systemd gettext gtk+ json-glib polkit krb5 libpam"

do_install_append() {
    # Resolve conflict: firewalld-0.6.3 already provides "/usr/lib/firewalld/services/cockpit.xml"
    rm -rf ${D}${libdir}/firewalld/services

    pkgdatadir=${datadir}/cockpit

    # fix up install location of these files
    cp -al ${D}${pkgdatadir}/dist/* ${D}/${pkgdatadir}
    rm -rf ${D}${pkgdatadir}/dist

    # remove unwanted artifacts
    rm -rf ${D}${pkgdatadir}/branding/{centos,debian,fedora,kubernetes,registry,rhel,ubuntu}

    rm -rf ${D}${pkgdatadir}/kdump
    rm -rf ${D}${pkgdatadir}/kubernetes
    rm -rf ${D}${pkgdatadir}/machines
    rm -rf ${D}${pkgdatadir}/ostree
    rm -rf ${D}${pkgdatadir}/packagekit
    rm -rf ${D}${pkgdatadir}/playground
    rm -rf ${D}${pkgdatadir}/realmd
    rm -rf ${D}${pkgdatadir}/selinux
    rm -rf ${D}${pkgdatadir}/sosreport
    rm -rf ${D}${pkgdatadir}/ssh
    rm -rf ${D}${pkgdatadir}/storaged
    rm -rf ${D}${pkgdatadir}/subscriptions
    rm -rf ${D}${pkgdatadir}/tuned

    chmod 4750 ${D}${libexecdir}/cockpit-session

    install -d "${D}${sysconfdir}/pam.d"
    install -p -m 644 ${WORKDIR}/cockpit.pam ${D}${sysconfdir}/pam.d/cockpit

#    install -d ${D}${datadir}/cockpit/branding
#    cp -r ${WORKDIR}/poky-aero ${D}${datadir}/cockpit/branding
}

RDEPENDS_${PN} = "bash"
RDEPENDS_${PN}-ws = "glib-networking"

